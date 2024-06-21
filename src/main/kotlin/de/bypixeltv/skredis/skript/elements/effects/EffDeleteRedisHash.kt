package de.bypixeltv.skredis.skript.elements.effects

import ch.njol.skript.Skript
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import de.bypixeltv.skredis.Main
import org.bukkit.event.Event

@Suppress("unused")
class EffDeleteRedisHash : Effect() {

    companion object{
        init {
            Skript.registerEffect(EffDeleteRedisHash::class.java, "delete field %string% in redis (hash|value) %string%")
        }
    }

    private var fieldName: Expression<String>? = null
    private var hashKey: Expression<String>? = null

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<Expression<*>>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parser: SkriptParser.ParseResult
    ): Boolean {
        this.fieldName = expressions[0] as Expression<String>
        this.hashKey = expressions[1] as Expression<String>
        return true
    }

    override fun toString(event: Event?, debug: Boolean): String {
        return "delete field ${this.fieldName} in redis hash ${this.hashKey}"
    }

    override fun execute(e: Event?) {
        val plugin = Main.INSTANCE

        val fieldName = fieldName!!.getSingle(e)
        if (fieldName == null) {
            plugin.sendErrorLogs("Redis hash field name was empty. Please check your code.")
            return
        }
        val hashKey = hashKey!!.getSingle(e)
        if (hashKey == null) {
            plugin.sendErrorLogs("Redis hash key was empty. Please check your code.")
            return
        }
        plugin.getRC()?.deleteHashField(hashKey, fieldName)
    }
}