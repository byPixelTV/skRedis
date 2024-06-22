package de.bypixeltv.skredis

import ch.njol.skript.Skript
import ch.njol.skript.SkriptAddon
import de.bypixeltv.skredis.commands.Commands
import de.bypixeltv.skredis.managers.RedisController
import de.bypixeltv.skredis.managers.RedisMessageManager
import de.bypixeltv.skredis.utils.IngameUpdateChecker
import de.bypixeltv.skredis.utils.UpdateChecker
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import net.axay.kspigot.main.KSpigot
import net.kyori.adventure.text.minimessage.MiniMessage
import java.io.IOException

class Main : KSpigot() {

    private val miniMessages = MiniMessage.miniMessage()
    private var redisController: RedisController? = null

    fun sendLogs(message: String) {
        this.server.consoleSender.sendMessage(miniMessages.deserialize("<grey>[<aqua>skRedis</aqua>]</grey> <yellow>$message</yellow>"))
    }

    fun sendErrorLogs(message: String) {
        this.server.consoleSender.sendMessage(miniMessages.deserialize("<grey>[<aqua>skRedis</aqua>]</grey> <red>$message</red>"))
    }

    fun getRC(): RedisController? {
        return redisController
    }

    private var instance: Main? = null
    private var addon: SkriptAddon? = null

    companion object {
        lateinit var INSTANCE: Main
    }

    init {
        instance = this
    }

    override fun load() {
        CommandAPI.onLoad(CommandAPIBukkitConfig(this).silentLogs(true).verboseOutput(true))
        Commands()
    }

    @Suppress("DEPRECATION")
    override fun startup() {
        saveDefaultConfig()

        INSTANCE = this
        this.instance = this
        this.addon = Skript.registerAddon(this)
        val localAddon = this.addon

        redisController = RedisController(this)
        var redisController: RedisController? = null

        IngameUpdateChecker

        val version = description.version
        if (version.contains("-")) {
            server.consoleSender.sendMessage(miniMessages.deserialize("<yellow>This is a BETA build, things may not work as expected, please report any bugs on GitHub</yellow>"))
            server.consoleSender.sendMessage(miniMessages.deserialize("<yellow>https://github.com/byPixelTV/skRedis/issues</yellow>"))
        }

        UpdateChecker.checkForUpdate(version)
        RedisMessageManager

        server.consoleSender.sendMessage(miniMessages.deserialize("<gray>[<aqua>SkRedis</aqua>]</gray> <yellow>Successfully enabled skRedis!</yellow>"))

        try {
            localAddon?.loadClasses("de.bypixeltv.skredis.skript", "elements")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun shutdown() {
        if (redisController != null) {
            redisController!!.shutdown()
        }
        CommandAPI.onDisable()
    }
}