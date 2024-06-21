package de.bypixeltv.skredis.skript.elements.effects

import ch.njol.skript.Skript
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import de.bypixeltv.skredis.Main
import org.bukkit.event.Event

@Suppress("unused")
class EffDeleteRedisList : Effect() {

    companion object{
        init {
            Skript.registerEffect(EffDeleteRedisList::class.java, "delete redis (list|array) %string%")
        }
    }

    private var listName: Expression<String>? = null

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<Expression<*>>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parser: SkriptParser.ParseResult
    ): Boolean {
        this.listName = expressions[0] as Expression<String>
        return true
    }

    override fun toString(event: Event?, debug: Boolean): String {
        return "delete redis list ${this.listName}"
    }

    override fun execute(e: Event?) {
        val plugin = Main.INSTANCE

        val listName = this.listName?.getSingle(e) ?: return
        plugin.getRC()?.deleteList(listName)
    }
}