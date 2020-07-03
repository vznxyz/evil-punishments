package net.evilblock.punishments.user.message

import net.evilblock.pidgin.message.handler.IncomingMessageHandler
import net.evilblock.pidgin.message.listener.MessageListener
import com.google.gson.JsonObject
import net.evilblock.punishments.user.punishment.Punishment
import net.evilblock.punishments.user.punishment.PunishmentType
import mkremins.fanciful.FancyMessage
import net.evilblock.cubed.Cubed
import net.evilblock.cubed.util.TimeUtil
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.punishments.EvilPunishments
import net.evilblock.punishments.user.User
import net.evilblock.punishments.user.UserHandler
import org.apache.commons.lang.StringUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.util.*
import java.util.concurrent.TimeUnit

object UserMessageListeners : MessageListener {

    @IncomingMessageHandler("PUNISHMENT_UPDATE")
    fun onPunishmentUpdateMessage(json: JsonObject) {
        val uuid = UUID.fromString(json.get("uuid").asString)
        val punishmentId = UUID.fromString(json.get("punishment").asString)

        val user = EvilPunishments.instance.database.fetchUser(uuid, false)!!

        if (UserHandler.isUserLoaded(uuid)) {
            UserHandler.cacheUser(user)
        }

        user.getPunishments().firstOrNull { it.uuid == punishmentId }?.also {
            executePunishment(user, it, json.get("silent").asBoolean)
        }
    }

    private fun executePunishment(punishedUser: User, punishment: Punishment, silent: Boolean) {
        val punishedUsername = punishedUser.getUsername()

        val issuerName: String = if (punishment.removedAt != null) {
            if (punishment.removedBy == null) {
                "${ChatColor.DARK_RED}Console"
            } else {
                Cubed.instance.uuidCache.name(punishment.removedBy!!)
            }
        } else {
            if (punishment.issuedBy == null) {
                "${ChatColor.DARK_RED}Console"
            } else {
                Cubed.instance.uuidCache.name(punishment.issuedBy!!)
            }
        }

        val silently = if (silent) {
            "${ChatColor.YELLOW}silently "
        } else {
            ""
        }

        val context = when {
            punishment.removalReason != null -> "un"
            punishment.punishmentType == PunishmentType.BLACKLIST -> ""
            punishment.expiresAt == null -> "permanently "
            else -> "temporarily "
        }

        val tooltip = arrayListOf<FancyMessage>(
            FancyMessage("Issued by: ")
                .color(ChatColor.YELLOW)
                .then(issuerName),
            FancyMessage("Reason: ")
                .color(ChatColor.YELLOW)
                .then(punishment.reason)
                .color(ChatColor.RED)
        )

        if (punishment.removalReason == null) {
            val durationText = if (punishment.expiresAt == null) {
                "Permanent"
            } else {
                TimeUtil.formatIntoDetailedString(TimeUnit.MILLISECONDS.toSeconds(punishment.expiresAt!! - System.currentTimeMillis()).toInt())
            }

            tooltip.add(
                FancyMessage("Duration: ")
                .color(ChatColor.YELLOW)
                .then(durationText)
                .color(ChatColor.RED)
            )
        }

        val message = if (punishment.punishmentType == PunishmentType.WARN) {
            "$punishedUsername ${ChatColor.GREEN}was ${punishment.punishmentType.action} by $issuerName${ChatColor.GREEN}."
        } else {
            "$punishedUsername ${ChatColor.GREEN}was $silently${ChatColor.GREEN}$context${punishment.punishmentType.action} by $issuerName${ChatColor.GREEN}."
        }

        val staffMessage = FancyMessage(message)
            .formattedTooltip(tooltip)

        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission(punishment.getViewPermission())) {
                staffMessage.send(onlinePlayer)
            } else if (!silent) {
                onlinePlayer.sendMessage(message)
            }
        }

        if (punishment.removalReason == null) {
            if (punishment.punishmentType.kick) {
                val kickedPlayers = Bukkit.getOnlinePlayers().filter { it.uniqueId == punishedUser.uuid || punishedUser.getIpAddresses().contains(it.address.address.hostAddress) }
                for (kickedPlayer in kickedPlayers) {
                    Tasks.sync {
                        kickedPlayer.kickPlayer(ChatColor.translateAlternateColorCodes('&', StringUtils.join(punishment.punishmentType.kickMessages, "\n")))
                    }
                }
            } else if (punishment.punishmentType == PunishmentType.WARN) {
                val player = Bukkit.getPlayer(punishedUser.uuid)
                if (player != null) {
                    player.sendMessage("${ChatColor.RED}${ChatColor.BOLD}You have been warned!")
                    player.sendMessage("${ChatColor.RED}Reason: ${punishment.reason}")
                }
            }
        }
    }

}