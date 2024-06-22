package de.bypixeltv.skredis.managers

import de.bypixeltv.skredis.Main
import redis.clients.jedis.JedisPubSub

object RedisMessageManager {
    private val redis = Main.INSTANCE.getRC()?.getJedisPool()

    init {
        val jedisPubSub = object : JedisPubSub() {
            override fun onPMessage(pattern: String, channel: String, message: String) {
                Main.INSTANCE.sendLogs("onPMessage called with pattern: $pattern, channel: $channel and message: $message")
                Main.INSTANCE.getRC()?.processMessage(channel, message)
            }

            override fun onPSubscribe(pattern: String, subscribedChannels: Int) {
                Main.INSTANCE.sendLogs("onPSubscribe called with pattern: $pattern and subscribedChannels: $subscribedChannels")
                super.onPSubscribe(pattern, subscribedChannels)
            }
        }

        // Run the subscription in a new thread
        Thread {
            Main.INSTANCE.sendLogs("Starting subscription thread...")
            try {
                redis?.resource?.use { jedis ->
                    Main.INSTANCE.sendLogs("Subscribing to Redis...")
                    jedis.psubscribe(jedisPubSub, "*")
                }
            } catch (e: Exception) {
                Main.INSTANCE.sendErrorLogs("Error while subscribing to Redis: ${e.message}")
                e.printStackTrace()
            }
        }.start()
    }
}