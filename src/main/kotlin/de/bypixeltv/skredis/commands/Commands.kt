package de.bypixeltv.skredis.commands

import ch.njol.skript.Skript
import ch.njol.skript.util.Version
import com.google.gson.Gson
import com.google.gson.JsonObject
import de.bypixeltv.skredis.Main
import de.bypixeltv.skredis.managers.RedisMessageManager
import de.bypixeltv.skredis.utils.UpdateChecker
import de.bypixeltv.skredis.utils.UpdateChecker.Companion.getLatestReleaseVersion
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

class Commands : CommandExecutor, TabCompleter {
    private val miniMessages = MiniMessage.miniMessage()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val adventureSender: Audience? = when (sender) {
            is ConsoleCommandSender -> Main.INSTANCE.getAdventure()?.console()
            is Player -> Main.INSTANCE.getAdventure()?.player(sender)
            else -> null
        }

        if (command.name.equals("skredis", ignoreCase = true)) {
            if (args.isEmpty()) {
                sender.sendMessage("Usage: /skredis <info|version|reload|reloadredis>")
                return true
            }

            when (args[0].lowercase()) {
                "info" -> {
                    if (sender.hasPermission("skredis.admin.info")) {
                        val addonMessages = Skript.getAddons().mapNotNull { addon ->
                            val name = addon.name
                            if (!name.contains("SkRedis")) {
                                "<grey>-</grey> <aqua>$name</aqua> <yellow>v${addon.plugin.description.version}</yellow>"
                            } else {
                                null
                            }
                        }

                        val addonsList =
                            if (addonMessages.isNotEmpty()) addonMessages.joinToString("\n") else "<color:#ff0000>No other addons found</color>"
                        adventureSender?.sendMessage(
                            miniMessages.deserialize(
                                "<dark_grey>--- <aqua>SkRedis</aqua> <grey>Info:</grey> ---</dark_grey>\n\n<grey>SkRedis Version: <aqua>${Main.INSTANCE.description.version}</aqua>\nSkript Version: <aqua>${Skript.getInstance().description.version}</aqua>\nServer Version: <aqua>${Main.INSTANCE.server.version}</aqua>\nServer Implementation: <aqua>${Main.INSTANCE.server.version}</aqua>\nAddons:\n$addonsList</grey>"
                            )
                        )
                    } else {
                        adventureSender?.sendMessage(miniMessages.deserialize("<dark_grey>[<gradient:blue:aqua:blue>SkRedis</gradient>]</dark_grey> <red>You do not have permission to use this command.</red>"))
                    }
                }
                "version" -> {
                    if (sender.hasPermission("skredis.admin.version")) {
                        val currentVersion = Main.INSTANCE.description.version
                        val updateVersion = UpdateChecker(Main.INSTANCE).getUpdateVersion(currentVersion)

                        getLatestReleaseVersion { version ->
                            val plugVer = Version(Main.INSTANCE.description.version)
                            val curVer = Version(version)
                            val url = URL("https://api.github.com/repos/byPixelTV/skRedis/releases/latest")
                            val reader = BufferedReader(InputStreamReader(url.openStream()))
                            val jsonObject = Gson().fromJson(reader, JsonObject::class.java)
                            var tagName = jsonObject["tag_name"].asString
                            tagName = tagName.removePrefix("v")
                            if (curVer <= plugVer) {
                                adventureSender?.sendMessage(miniMessages.deserialize("<dark_grey>[<gradient:blue:aqua:blue>SkRedis</gradient>]</dark_grey> <green>The plugin is up to date!</green>"))
                            } else {
                                Bukkit.getScheduler().runTaskLater(Main.INSTANCE, Runnable {
                                    updateVersion.thenApply { version ->
                                        adventureSender?.sendMessage(miniMessages.deserialize("<dark_grey>[<gradient:blue:aqua:blue>SkRedis</gradient>]</dark_grey> update available: <green>$version</green>"))
                                        adventureSender?.sendMessage(miniMessages.deserialize("<dark_grey>[<gradient:blue:aqua:blue>SkRedis</gradient>]</dark_grey> download at <aqua><click:open_url:'https://github.com/byPixelTV/SkRedis/releases'>https://github.com/byPixelTV/SkRedis/releases</click></aqua>"))
                                        true
                                    }
                                }, 30)
                            }
                        }
                    } else {
                        adventureSender?.sendMessage(miniMessages.deserialize("<dark_grey>[<gradient:blue:aqua:blue>SkRedis</gradient>]</dark_grey> <red>You do not have permission to use this command.</red>"))
                    }
                }
                "reload" -> {
                    if (sender.hasPermission("skredis.admin.reload")) {
                        Main.INSTANCE.reloadConfig()
                        val path = Paths.get("/plugins/SkRedis/config.yml")
                        if (Files.exists(path)) {
                            Main.INSTANCE.saveConfig()
                        } else {
                            Main.INSTANCE.saveDefaultConfig()
                        }

                        adventureSender?.sendMessage(miniMessages.deserialize("<dark_grey>[<gradient:blue:aqua:blue>SkRedis</gradient>]</dark_grey> <color:#43fa00>Successfully reloaded the config!</color>"))
                    } else {
                        adventureSender?.sendMessage(miniMessages.deserialize("<dark_grey>[<gradient:blue:aqua:blue>SkRedis</gradient>]</dark_grey> <red>You do not have permission to use this command.</red>"))
                    }
                }
                "reloadredis" -> {
                    if (sender.hasPermission("skredis.admin.reloadredis")) {
                        Main.INSTANCE.reloadConfig()
                        val path = Paths.get("/plugins/SkRedis/config.yml")
                        if (Files.exists(path)) {
                            Main.INSTANCE.saveConfig()
                        } else {
                            Main.INSTANCE.saveDefaultConfig()
                        }

                        try {
                            RedisMessageManager.reloadRedisConnection()
                            adventureSender?.sendMessage(miniMessages.deserialize("<dark_grey>[<gradient:blue:aqua:blue>SkRedis</gradient>]</dark_grey> <color:#43fa00>Successfully reloaded the redis connection!</color>"))
                        } catch (e: Exception) {
                            adventureSender?.sendMessage(miniMessages.deserialize("<dark_grey>[<gradient:blue:aqua:blue>SkRedis</gradient>]</dark_grey> <red>Failed to reload the Redis connection!</red>"))
                            e.printStackTrace()
                        }
                    } else {
                        adventureSender?.sendMessage(miniMessages.deserialize("<dark_grey>[<gradient:blue:aqua:blue>SkRedis</gradient>]</dark_grey> <red>You do not have permission to use this command.</red>"))
                    }
                }
                else -> {
                    adventureSender?.sendMessage(miniMessages.deserialize("<dark_grey>[<gradient:blue:aqua:blue>SkRedis</gradient>]</dark_grey> <red>Usage: skredis <info|version|reload|reloadredis></red>"))
                }
            }
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String>? {
        if (command.name.equals("skredis", ignoreCase = true)) {
            if (args.size == 1) {
                val completions = listOf("info", "version", "reload", "reloadredis")
                return completions.filter { it.startsWith(args[0], ignoreCase = true) }
            }
        }
        return null
    }
}