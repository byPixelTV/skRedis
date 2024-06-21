package de.bypixeltv.skredis.skript.elements.effects

import ch.njol.skript.Skript
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import de.bypixeltv.skredis.Main
import org.bukkit.event.Event

@Suppress("unused")
class EffDeleteRedisString : Effect() {

    companion object{
        init {
            Skript.registerEffect(EffDeleteRedisString::class.java, "delete redis string %string%")
        }
    }

    private var stringName: Expression<String>? = null

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<Expression<*>>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parser: SkriptParser.ParseResult
    ): Boolean {
        this.stringName = expressions[0] as Expression<String>
        return true
    }

    override fun toString(event: Event?, debug: Boolean): String {
        return "delete redis string ${this.stringName}"
    }

    override fun execute(e: Event?) {
        val plugin = Main.INSTANCE

        val stringName = this.stringName?.getSingle(e) ?: return
        plugin.getRC()?.deleteString(stringName)
    }
}