package de.bypixeltv.skredis.managers

import de.bypixeltv.skredis.Main
import redis.clients.jedis.JedisPubSub

object RedisMessageManager {
    private val redis = Main.INSTANCE.getRC()?.getJedisPool()

    init {
        val channels = mutableListOf("redisbungee-data", "redisvelocity-players")
        val cchannels = Main.INSTANCE.config.getStringList("Channels")
        channels.addAll(cchannels)

        val jedisPubSub = object : JedisPubSub() {
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
}