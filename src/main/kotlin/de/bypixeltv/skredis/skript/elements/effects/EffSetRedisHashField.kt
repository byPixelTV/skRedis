package de.bypixeltv.skredis.skript.elements.effects

import ch.njol.skript.Skript
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import de.bypixeltv.skredis.Main
import org.bukkit.event.Event

@Suppress("unused")
class EffSetRedisHashField : Effect() {

    companion object{
        init {
            Skript.registerEffect(EffSetRedisHashField::class.java, "set field %strings% to %string% in redis (hash|value) %string%")
        }
    }

    private var hashName: Expression<String>? = null
    private var fieldName: Expression<String>? = null
    private var fieldValue: Expression<String>? = null

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<Expression<*>>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parser: SkriptParser.ParseResult
    ): Boolean {
        this.fieldName = expressions[0] as Expression<String>
        this.fieldValue = expressions[1] as Expression<String>
        this.hashName = expressions[2] as Expression<String>
        return true
    }

    override fun toString(event: Event?, debug: Boolean): String {
        return "set redis hash ${this.hashName}"
    }

    override fun execute(e: Event?) {
        val plugin = Main.INSTANCE

        val hashName = hashName?.getSingle(e)
        val fieldName = fieldName?.getSingle(e)
        val fieldValue = fieldValue?.getSingle(e)

        if (hashName == null) {
            plugin.sendErrorLogs("HashName was empty. Please check your code.")
            return
        }
        if (fieldName == null) {
            plugin.sendErrorLogs("FieldName was empty. Please check your code.")
            return
        }
        if (fieldValue == null) {
            plugin.sendErrorLogs("FieldValue was empty. Please check your code.")
            return
        } else {
            plugin.getRC()?.setHashField(hashName, fieldName, fieldValue)
        }
    }
}