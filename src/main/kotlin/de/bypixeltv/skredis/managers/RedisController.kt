package de.bypixeltv.skredis.managers

import de.bypixeltv.skredis.Main
import de.bypixeltv.skredis.events.RedisMessageEvent
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import org.json.JSONObject
import redis.clients.jedis.BinaryJedisPubSub
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class RedisController(private val plugin: Main) : BinaryJedisPubSub(), Runnable {

    private val jedisPool: JedisPool
    private var channelsInByte: Array<ByteArray>
    private val isConnectionBroken = AtomicBoolean(true)
    private val isConnecting = AtomicBoolean(false)
    private val connectionTask: BukkitTask
    val config = plugin.config

    init {
        val jConfig = JedisPoolConfig()
        val maxConnections = 10

        jConfig.maxTotal = maxConnections
        jConfig.maxIdle = maxConnections
        jConfig.minIdle = 1
        jConfig.blockWhenExhausted = true

        val password = config.getString("redis.password") ?: ""
        jedisPool = if (password.isEmpty()) {
            JedisPool(
                jConfig,
                config.getString("redis.host") ?: "127.0.0.1",
                config.getInt("redis.port"),
                config.getInt("redis.timeout"),
                config.getBoolean("redis.ssl")
            )
        } else {
            JedisPool(
                jConfig,
                config.getString("redis.host") ?: "127.0.0.1",
                config.getInt("redis.port"),
                config.getInt("redis.timeout"),
                password,
                config.getBoolean("redis.ssl")
            )
        }

        channelsInByte = setupChannels()
        connectionTask = plugin.server.scheduler.runTaskTimerAsynchronously(plugin, this, 0, 20 * 5)
    }

    override fun run() {
        if (!isConnectionBroken.get() || isConnecting.get()) {
            return
        }
        plugin.sendLogs("Connecting to Redis server...")
        isConnecting.set(true)
        try {
            jedisPool.resource.use { jedis ->
                isConnectionBroken.set(false)
                plugin.sendLogs("Connection to Redis server has established! Success!")
                jedis.subscribe(this, *channelsInByte)
            }
        } catch (e: Exception) {
            isConnecting.set(false)
            isConnectionBroken.set(true)
            plugin.sendErrorLogs("Connection to Redis server has failed! Please check your details in the configuration.")
            e.printStackTrace()
        }
    }

    fun shutdown() {
        connectionTask.cancel()
        if (this.isSubscribed) {
            try {
                this.unsubscribe()
            } catch (e: Exception) {
                plugin.sendErrorLogs("Something went wrong during unsubscribing...")
                e.printStackTrace()
            }
        }
        jedisPool.close()
    }

    override fun onMessage(channel: ByteArray, message: ByteArray) {
        val channelString = String(channel, StandardCharsets.UTF_8)
        var receivedMessage: String? = null
        try {
            receivedMessage = String(message, StandardCharsets.UTF_8)
            if (!receivedMessage.startsWith("{") || !receivedMessage.endsWith("}")) {
                if (plugin.config.getBoolean("RediVelocity.enabled")) {
                    val event = RedisMessageEvent(channelString, receivedMessage, System.currentTimeMillis())
                    if (plugin.isEnabled) {
                        Bukkit.getScheduler().runTask(plugin, Runnable { plugin.server.pluginManager.callEvent(event) })
                    }
                }
            } else {
                val j = JSONObject(receivedMessage)

                if (plugin.config.getBoolean("RediVelocity.enabled")) {
                    when (j.getString("action")) {
                        "serverSwitch", "postLogin", "disconnect" -> {
                            val messages = j.getString("target")
                            val date = System.currentTimeMillis()
                            val event = RedisMessageEvent(channelString, "redisbungee:JOIN;$messages", date)
                            if (plugin.isEnabled) {
                                Bukkit.getScheduler().runTask(plugin, Runnable { plugin.server.pluginManager.callEvent(event) })
                            }
                        }
                        "Skript" -> {
                            val messages = j.getJSONArray("Messages")
                            for (i in 0 until messages.length()) {
                                val event = RedisMessageEvent(channelString, messages.getString(i), j.getLong("Date"))
                                if (plugin.isEnabled) {
                                    Bukkit.getScheduler().runTask(plugin, Runnable { plugin.server.pluginManager.callEvent(event) })
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            plugin.sendErrorLogs("I got a message that was empty from channel $channelString please check your code that you used to send the message. Message content:")
            if (receivedMessage != null) {
                plugin.sendErrorLogs(receivedMessage)
            }
            e.printStackTrace()
        }
    }

    fun sendMessage(message: String, channel: String) {
        val jsonObject = JSONObject()
        jsonObject.put("Messages", message) // Send the message as a single string
        jsonObject.put("action", "CUtils")
        jsonObject.put("Date", System.currentTimeMillis())

        val jsonString = jsonObject.toString()

        // Publish the JSON string to the specified channel
        jedisPool.resource.use { jedis ->
            jedis.publish(channel, jsonString)
        }
    }

    fun setHashField(hashName: String, fieldName: String, value: String) {
        jedisPool.resource.use { jedis ->
            val type = jedis.type(hashName)
            if (type != "hash") {
                if (type == "none") {
                    jedis.hset(hashName, fieldName, value)
                } else {
                    System.err.println("Error: Key $hashName doesn't hold a hash. It holds a $type.")
                }
            } else {
                jedis.hset(hashName, fieldName, value)
            }
        }
    }

    fun deleteHashField(hashName: String, fieldName: String) {
        jedisPool.resource.use { jedis ->
            jedis.hdel(hashName, fieldName)
        }
    }

    fun deleteHash(hashName: String) {
        jedisPool.resource.use { jedis ->
            jedis.del(hashName)
        }
    }

    fun addToList(listName: String, values: Array<String>) {
        jedisPool.resource.use { jedis ->
            values.forEach { value ->
                jedis.rpush(listName, value)
            }
        }
    }

    fun setListValue(listName: String, index: Int, value: String) {
        jedisPool.resource.use { jedis ->
            val listLength = jedis.llen(listName)
            if (index >= listLength) {
                System.err.println("Error: Index $index does not exist in the list $listName.")
            } else {
                jedis.lset(listName, index.toLong(), value)
            }
        }
    }

    fun removeFromList(listName: String, index: Int) {
        jedisPool.resource.use { jedis ->
            val listLength = jedis.llen(listName)
            if (index >= listLength) {
                System.err.println("Error: Index $index does not exist in the list $listName.")
            } else {
                val tempKey = UUID.randomUUID().toString()
                jedis.lset(listName, index.toLong(), tempKey)
                jedis.lrem(listName, 0, tempKey)
            }
        }
    }

    fun deleteList(listName: String) {
        jedisPool.resource.use { jedis ->
            jedis.del(listName)
        }
    }

    fun setString(key: String, value: String) {
        jedisPool.resource.use { jedis ->
            jedis.set(key, value)
        }
    }

    fun getString(key: String): String? {
        return jedisPool.resource.use { jedis ->
            jedis.get(key)
        }
    }

    fun deleteString(key: String) {
        jedisPool.resource.use { jedis ->
            jedis.del(key)
        }
    }

    fun getHashField(hashName: String, fieldName: String): String? {
        return jedisPool.resource.use { jedis ->
            jedis.hget(hashName, fieldName)
        }
    }

    fun getAllHashFields(hashName: String): Set<String>? {
        return jedisPool.resource.use { jedis ->
            jedis.hkeys(hashName)
        }
    }

    fun getAllHashValues(hashName: String): List<String>? {
        return jedisPool.resource.use { jedis ->
            jedis.hvals(hashName)
        }
    }

    fun getList(listName: String): List<String>? {
        return jedisPool.resource.use { jedis ->
            jedis.lrange(listName, 0, -1)
        }
    }

    fun getHashFieldNamesByValue(hashName: String, value: String): List<String> {
        val fieldNames = mutableListOf<String>()
        jedisPool.resource.use { jedis ->
            val keys = jedis.keys(hashName)
            for (key in keys) {
                val fieldsAndValues = jedis.hgetAll(key)
                for (entry in fieldsAndValues.entries) {
                    if (entry.value == value) {
                        fieldNames.add(entry.key)
                    }
                }
            }
        }
        return fieldNames
    }


    private fun setupChannels(): Array<ByteArray> {
        val channels = config.getString("redis.channels")?.split(", ") ?: emptyList()
        return Array(channels.size) { channels[it].toByteArray(StandardCharsets.UTF_8) }
    }

    fun isRedisConnectionOffline(): Boolean {
        return isConnectionBroken.get()
    }

    fun getJedisPool(): JedisPool {
        return jedisPool
    } //

}