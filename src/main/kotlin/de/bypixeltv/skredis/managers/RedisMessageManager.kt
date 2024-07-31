package de.bypixeltv.skredis.managers

import de.bypixeltv.skredis.Main
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.JedisPubSub

object RedisMessageManager {
    private var redis = Main.INSTANCE.getRC()?.getJedisPool()
    private var jedisPubSub: JedisPubSub? = null
    private var channels = mutableListOf("redisbungee-data", "redisvelocity-players")

    init {
        val cchannels = Main.INSTANCE.config.getStringList("channels")
        channels.addAll(cchannels)

        jedisPubSub = object : JedisPubSub() {
            override fun onPMessage(pattern: String, channel: String, message: String) {
                if (channels.contains(channel)) {
                    Main.INSTANCE.getRC()?.processMessage(channel, message)
                }
            }

            override fun onPSubscribe(pattern: String, subscribedChannels: Int) {
                super.onPSubscribe(pattern, subscribedChannels)
            }
        }

        // Run the subscription in a new thread
        Thread {
            try {
                redis?.resource?.use { jedis ->
                    jedis.psubscribe(jedisPubSub, *channels.toTypedArray())
                }
            } catch (e: Exception) {
                Main.INSTANCE.sendErrorLogs("Error while subscribing to Redis: ${e.message}")
                e.printStackTrace()
            }
        }.start()
    }

    fun reloadRedisConnection() {
        // Unsubscribe from all channels
        jedisPubSub?.unsubscribe()

        // Close the current JedisPool
        redis?.close()

        // Create a new JedisPool
        val jConfig = JedisPoolConfig()
        val maxConnections = 10

        jConfig.maxTotal = maxConnections
        jConfig.maxIdle = maxConnections
        jConfig.minIdle = 1
        jConfig.blockWhenExhausted = true

        val password = Main.INSTANCE.config.getString("redis.password") ?: ""
        redis = if (password.isEmpty()) {
            JedisPool(
                jConfig,
                Main.INSTANCE.config.getString("redis.host") ?: "127.0.0.1",
                Main.INSTANCE.config.getInt("redis.port"),
                Main.INSTANCE.config.getInt("redis.timeout"),
                Main.INSTANCE.config.getBoolean("redis.usessl")
            )
        } else {
            JedisPool(
                jConfig,
                Main.INSTANCE.config.getString("redis.host") ?: "127.0.0.1",
                Main.INSTANCE.config.getInt("redis.port"),
                Main.INSTANCE.config.getInt("redis.timeout"),
                password,
                Main.INSTANCE.config.getBoolean("redis.usessl")
            )
        }

        // Re-subscribe to the channels
        Thread {
            try {
                redis?.resource?.use { jedis ->
                    jedis.psubscribe(jedisPubSub, *channels.toTypedArray())
                }
            } catch (e: Exception) {
                Main.INSTANCE.sendErrorLogs("Error while subscribing to Redis: ${e.message}")
                e.printStackTrace()
            }
        }.start()
    }
}