package de.bypixeltv.skredis.skript.elements.effects

import ch.njol.skript.Skript
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import de.bypixeltv.skredis.Main
import org.bukkit.event.Event

@Suppress("unused")
class EffSetRedisString : Effect() {

    companion object{
        init {
            Skript.registerEffect(EffDeleteRedisString::class.java, "set redis string %string% to %string%")
        }
    }

    private var stringName: Expression<String>? = null
    private var stringValue: Expression<String>? = null

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<Expression<*>>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parser: SkriptParser.ParseResult
    ): Boolean {
        this.stringName = expressions[0] as Expression<String>
        this.stringValue = expressions[1] as Expression<String>
        return true
    }

    override fun toString(event: Event?, debug: Boolean): String {
        return "set redis string ${this.stringName} to ${this.stringValue}"
    }

    override fun execute(e: Event?) {
        val plugin = Main.INSTANCE

        val name = stringName!!.getSingle(e)
        if (name == null) {
            plugin.sendErrorLogs("Redis string name was empty. Please check your code.")
            return
        }
        val value = stringValue!!.getSingle(e)
        if (value == null) {
            plugin.sendErrorLogs("Redis string value was empty. Please check your code.")
            return
        }
        plugin.getRC()!!.setString(name, value)
    }
}