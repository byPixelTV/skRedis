package de.bypixeltv.skredis.skript.elements.expressions

import ch.njol.skript.Skript
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.util.Kleenean
import de.bypixeltv.skredis.Main
import org.bukkit.event.Event

@Suppress("unused")
class ExprGetRedisList : SimpleExpression<String>() {

    companion object{
        init {
            Skript.registerExpression(
                ExprGetRedisList::class.java, String::class.java,
                ExpressionType.SIMPLE, "redis (array|list) %string%")
        }
    }

    private var listKey: Expression<String>? = null

    override fun isSingle(): Boolean {
        return false
    }

    @Suppress("UNCHECKED_CAST")
    override fun init(
        exprs: Array<Expression<*>>,
        matchedPattern: Int,
        isDelayed: Kleenean?,
        parseResult: SkriptParser.ParseResult?
    ): Boolean {
        this.listKey = exprs[0] as Expression<String>? ?: return false
        return true
    }

    override fun get(e: Event?): Array<String>? {
        val plugin = Main.INSTANCE

        val redisListName: String? = listKey?.getSingle(e)
        if (redisListName != null) {
            return plugin.getRC()?.getList(redisListName)?.toTypedArray()
        }
        return null
    }

    override fun getReturnType(): Class<out String> {
        return String::class.java
    }

    override fun toString(event: Event?, b: Boolean): String {
        return "redis list ${this.listKey}"
    }

}