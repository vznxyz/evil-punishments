package net.evilblock.punishments.user.listener

import net.evilblock.cubed.Cubed
import net.evilblock.cubed.logging.ErrorHandler
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.punishments.EvilPunishments
import net.evilblock.punishments.user.UserHandler
import net.evilblock.punishments.user.punishment.PunishmentType
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.lang.Exception

object UserListeners : Listener {

    @EventHandler
    fun onAsyncPlayerPreLoginEvent(event: AsyncPlayerPreLoginEvent) {
        // we need to check if player is still logged in when receiving another login attempt
        // this happens when a player using a custom client that can access the server list while in-game (and reconnecting)
        val player = Bukkit.getPlayer(event.uniqueId)
        if (player != null && player.isOnline) {
            event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
            event.kickMessage = "${ChatColor.RED}You tried to login too quickly after disconnecting.\nTry again in a few seconds."

            Tasks.sync {
                player.kickPlayer("${ChatColor.RED}Duplicate login kick")
            }

            return
        }

        try {
            val punishment = EvilPunishments.instance.database.fetchActivePunishment(event.uniqueId, event.address.hostAddress)
            if (punishment != null) {
                var kickMessage = if (punishment.second.punishmentType == PunishmentType.BAN) {
                    "${ChatColor.RED}Your account has been suspended from MineJunkie.\nCreate a support ticket on our site to appeal."
                } else {
                    "${ChatColor.RED}Your account has been blacklisted from MineJunkie.\nThis type of punishment can't be appealed."
                }

                if (event.uniqueId != punishment.first) {
                    val relatedUsername = Cubed.instance.uuidCache.name(punishment.first)
                    kickMessage += "\nThis punishment is in relation to ${ChatColor.YELLOW}$relatedUsername${ChatColor.RED}."
                }

                event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_BANNED
                event.kickMessage = kickMessage
            }
        } catch (e: Exception) {
            EvilPunishments.instance.logger.info("Failed to search for active punishment for " + event.name + ":")
            e.printStackTrace()
            return
        }

        try {
            val user = EvilPunishments.instance.database.fetchUser(event.uniqueId, true)!!
            if (!user.getIpAddresses().contains(event.address.hostAddress)) {
                user.addIpAddress(event.address.hostAddress)
                EvilPunishments.instance.database.saveUser(user)
            }

            UserHandler.cacheUser(user)
        } catch (exception: Exception) {
            val eventDetails = mapOf(
                "Player Name" to event.name,
                "Player UUID" to event.uniqueId.toString(),
                "Player IP" to event.address.hostAddress
            )

            val logId = ErrorHandler.generateErrorLog("loginEvent", eventDetails, exception)

            val kickMessage = StringBuilder()
                .append("${ChatColor.RED}${ChatColor.BOLD}Sorry about that...")
                .append("\n")
                .append("${ChatColor.GRAY}We failed to load your user data. Please try again later.")
                .append("\n")
                .append("${ChatColor.GRAY}If this error persists, please contact an admin and")
                .append("\n")
                .append("${ChatColor.GRAY}provide them this error ID: ${ChatColor.WHITE}$logId")

            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, kickMessage.toString())
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onAsyncPlayerChatEventLow(event: AsyncPlayerChatEvent) {
        val user = UserHandler.getUser(event.player.uniqueId)
        if (user == null) {
            event.isCancelled = true
            event.player.sendMessage("${ChatColor.RED}You can't chat because your punishments data hasn't been loaded!")
            return
        }

        val activeMute = user.getActivePunishment(PunishmentType.MUTE)
        if (activeMute != null) {
            event.isCancelled = true

            if (activeMute.isPermanent()) {
                event.player.sendMessage("${ChatColor.RED}You're permanently muted.")
            } else {
                event.player.sendMessage("${ChatColor.RED}You're muted for another ${activeMute.getFormattedRemainingTime()}.")
            }

            event.player.sendMessage("${ChatColor.RED}Reason: ${activeMute.reason}")
        }
    }

    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        UserHandler.getUser(event.player.uniqueId)?.let {
            UserHandler.forgetUser(it)
        }
    }

}