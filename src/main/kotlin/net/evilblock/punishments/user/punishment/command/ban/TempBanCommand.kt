package net.evilblock.punishments.user.punishment.command.ban

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.flag.Flag
import net.evilblock.cubed.command.data.parameter.Param
import net.evilblock.cubed.util.TimeUtil
import net.evilblock.pidgin.message.Message
import net.evilblock.punishments.EvilPunishments
import net.evilblock.punishments.user.User
import net.evilblock.punishments.user.punishment.Punishment
import net.evilblock.punishments.user.punishment.PunishmentType
import net.evilblock.punishments.util.Permissions
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

object TempBanCommand {

    @Command(
        names = ["tempban"],
        description = "Temporarily ban a player",
        permission = Permissions.BAN,
        async = true
    )
    @JvmStatic
    fun execute(
        sender: CommandSender,
        @Flag(value = ["s", "silent"], description = "Silently ban the player") silent: Boolean,
        @Param(name = "player") user: User,
        @Param(name = "time") time: String,
        @Param(name = "reason", wildcard = true) reason: String
    ) {
        var issuer: UUID? = null
        if (sender is Player) {
            issuer = sender.uniqueId
        }

        if (user.getActivePunishment(PunishmentType.BAN) != null) {
            sender.sendMessage("${user.getUsername()} ${ChatColor.RED}is already banned.")
            return
        }

        val expiresAt: Long?

        try {
            val seconds = if (time.equals("perm", ignoreCase = true) || time.equals("permanent", ignoreCase = true)) {
                -1
            } else {
                TimeUtil.parseTime(time)
            }

            if (sender is Player && !sender.hasPermission(Permissions.BAN_PERMANENT)) {
                if (seconds == -1 || seconds > 7_862_400) {
                    sender.sendMessage("${ChatColor.RED}You don't have permission to create a ban this long. Maximum time allowed: 90 days.")
                }
            }

            expiresAt = if (seconds == -1) {
                null
            } else {
                System.currentTimeMillis() + (seconds * 1_000L)
            }
        } catch (e: Exception) {
            sender.sendMessage("${ChatColor.RED}Time provided is invalid.")
            return
        }

        val punishment = Punishment(UUID.randomUUID(), punishmentType = PunishmentType.BAN)
        punishment.reason = reason
        punishment.issuedBy = issuer
        punishment.expiresAt = expiresAt

        user.addPunishment(punishment)
        EvilPunishments.instance.database.saveUser(user)

        val message = Message("PUNISHMENT_UPDATE", mapOf(
            "uuid" to user.uuid.toString(),
            "punishment" to punishment.uuid.toString(),
            "silent" to silent
        ))

        EvilPunishments.instance.pidgin.sendMessage(message)
    }

}