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
class ExprGetValueOfFieldInHash : SimpleExpression<String>() {

    companion object{
        init {
            Skript.registerExpression(
                ExprGetValueOfFieldInHash::class.java, String::class.java,
                ExpressionType.SIMPLE, "value of field %string% in redis (hash|value) %string%")
        }
    }

    private var fieldName: Expression<String>? = null
    private var hashKey: Expression<String>? = null

    override fun isSingle(): Boolean {
        return true
    }

    @Suppress("UNCHECKED_CAST")
    override fun init(
        exprs: Array<Expression<*>>,
        matchedPattern: Int,
        isDelayed: Kleenean?,
        parseResult: SkriptParser.ParseResult?
    ): Boolean {
        this.fieldName = exprs[0] as Expression<String>? ?: return false
        this.hashKey = exprs[1] as Expression<String>? ?: return false
        return true
    }

    override fun get(e: Event?): Array<String>? {
        val plugin = Main.INSTANCE

        val hashName: String? = this.hashKey?.getSingle(e)
        val fieldName: String? = this.fieldName?.getSingle(e)

        if (hashName == null || fieldName == null) {
            return null
        }

        val hashValue: String? = plugin.getRC()?.getHashField(hashName, fieldName)
        return if (hashValue != null) arrayOf(hashValue) else null
    }

    override fun getReturnType(): Class<out String> {
        return String::class.java
    }

    override fun toString(event: Event?, b: Boolean): String {
        return "value of field ${this.fieldName} in redis hash ${this.hashKey}"
    }

}