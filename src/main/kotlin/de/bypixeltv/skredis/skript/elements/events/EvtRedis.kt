package de.bypixeltv.skredis.skript.elements.events

import ch.njol.skript.Skript
import ch.njol.skript.lang.Literal
import ch.njol.skript.lang.SkriptEvent
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.registrations.EventValues
import ch.njol.skript.util.Getter
import de.bypixeltv.skredis.events.RedisMessageEvent
import org.bukkit.event.Event
import java.util.Date

class EvtRedis : SkriptEvent() {

    companion object{
        init {
            Skript.registerEvent("redis message", EvtRedis::class.java, RedisMessageEvent::class.java, "redis message")

            EventValues.registerEventValue(RedisMessageEvent::class.java, String::class.java, object :
                Getter<String, RedisMessageEvent>() {
                override fun get(e: RedisMessageEvent): String {
                    return e.channelName
                }
            }, 0)

            EventValues.registerEventValue(RedisMessageEvent::class.java, String::class.java, object :
                Getter<String, RedisMessageEvent>() {
                override fun get(e: RedisMessageEvent): String {
                    return e.message
                }
            }, 0)

            EventValues.registerEventValue(RedisMessageEvent::class.java, Date::class.java, object :
                Getter<Date, RedisMessageEvent>() {
                override fun get(e: RedisMessageEvent): Date {
                    return Date(e.date)
                }
            }, 0)
        }
    }

    override fun init(literals: Array<Literal<*>?>?, i: Int, parseResult: SkriptParser.ParseResult?): Boolean {
        return true
    }

    override fun check(event: Event?): Boolean {
        return (event is RedisMessageEvent)
    }

    override fun toString(event: Event?, debug: Boolean): String {
        return "redis message"
    }

}