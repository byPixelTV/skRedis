package de.bypixeltv.skredis.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

@Suppress("unused")
class RedisMessageEvent(val channelName: String, val message: String, val date: Long) : Event(false) {
    override fun getEventName(): String {
        return super.getEventName()
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList: HandlerList = HandlerList()
    }
}