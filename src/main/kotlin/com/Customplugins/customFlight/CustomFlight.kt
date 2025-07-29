// ★ Crafted by Kin ★
// Plugin: SkySurge – Custom Flight Plugin (Kotlin Edition)

package com.kinscripts.skysurge

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class SkySurge : JavaPlugin(), Listener, TabExecutor {

    private val flightEnabled = mutableSetOf<UUID>()

    override fun onEnable() {
        logger.info("★ SkySurge is lifting off... ★")
        server.pluginManager.registerEvents(this, this)
        getCommand("flight")?.apply {
            setExecutor(this@SkySurge)
            tabCompleter = this@SkySurge
        }
    }

    override fun onDisable() {
        flightEnabled.forEach { uuid ->
            Bukkit.getPlayer(uuid)?.takeIf { it.isOnline }?.apply {
                allowFlight = false
                isFlying = false
            }
        }
        flightEnabled.clear()
        logger.info("★ SkySurge has landed. Flight disabled for all. ★")
    }

    private fun toggleFlight(player: Player) {
        val uuid = player.uniqueId

        if (flightEnabled.contains(uuid)) {
            player.isFlying = false
            player.allowFlight = false
            flightEnabled.remove(uuid)
            player.sendMessage(color("&c[SkySurge] &7Flight disabled."))
        } else {
            when (player.gameMode) {
                GameMode.SURVIVAL, GameMode.ADVENTURE -> {
                    player.allowFlight = true
                    player.isFlying = true
                    flightEnabled.add(uuid)
                    player.sendMessage(color("&a[SkySurge] &7Flight enabled."))
                }
                else -> {
                    player.sendMessage(color("&e[SkySurge] &7You're already flying in this gamemode."))
                }
            }
        }
    }

    private fun color(message: String): String =
        ChatColor.translateAlternateColorCodes('&', message)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Only players can toggle flight.")
            return true
        }

        val player = sender

        when (args.size) {
            0 -> {
                toggleFlight(player)
            }
            1 -> {
                if (!player.hasPermission("skysurge.toggle.others")) {
                    player.sendMessage(color("&c[SkySurge] &7You don’t have permission to toggle for others."))
                    return true
                }

                val target = Bukkit.getPlayerExact(args[0])
                if (target != null && target.isOnline) {
                    toggleFlight(target)
                    player.sendMessage(color("&b[SkySurge] &7Toggled flight for &e${target.name}"))
                } else {
                    player.sendMessage(color("&c[SkySurge] &7That player is not online."))
                }
            }
            else -> {
                player.sendMessage(color("&c[SkySurge] &7Usage: /flight [player]"))
            }
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String> {
        if (args.size == 1 && sender.hasPermission("skysurge.toggle.others")) {
            val partial = args[0].lowercase(Locale.getDefault())
            return Bukkit.getOnlinePlayers()
                .map { it.name }
                .filter { it.lowercase(Locale.getDefault()).startsWith(partial) }
                .toMutableList()
        }

        return mutableListOf()
    }
}
