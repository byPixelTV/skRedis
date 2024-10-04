package de.bypixeltv.skredis.utils

import de.bypixeltv.skredis.Main
import net.axay.kspigot.event.listen
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerJoinEvent

object IngameUpdateChecker {
    private val miniMessages = MiniMessage.miniMessage()

    @Suppress("DEPRECATION", "UNUSED")
    val joinEvent = listen<PlayerJoinEvent> {
        val player = it.player
        if (Main.INSTANCE.config.getBoolean("update-checker")) {
            if (player.hasPermission("skredis.admin.version") || player.isOp) {
                val currentVersion = Main.INSTANCE.description.version
                val updateVersion = UpdateChecker(Main.INSTANCE).getUpdateVersion(currentVersion)

                val adventurePlayer = Main.INSTANCE.getAdventure()?.player(player)

                Bukkit.getScheduler().runTaskLater(Main.INSTANCE, Runnable {
                    updateVersion.thenApply { version ->
                        adventurePlayer?.sendMessage(miniMessages.deserialize("<dark_grey>[<gradient:blue:aqua:blue>SkRedis</gradient>]</dark_grey> update available: <green>$version</green>"))
                        adventurePlayer?.sendMessage(miniMessages.deserialize("<dark_grey>[<gradient:blue:aqua:blue>SkRedis</gradient>]</dark_grey> download at <aqua><click:open_url:'https://github.com/byPixelTV/skRedis/releases'>https://github.com/byPixelTV/skRedis/releases</click></aqua>"))
                        true
                    }
                }, 30)
            }
        }
    }
}