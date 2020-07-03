package net.evilblock.punishments.user.punishment.command.mute

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.flag.Flag
import net.evilblock.cubed.command.data.parameter.Param
import net.evilblock.pidgin.message.Message
import net.evilblock.punishments.EvilPunishments
import net.evilblock.punishments.user.User
import net.evilblock.punishments.user.punishment.PunishmentType
import net.evilblock.punishments.util.Permissions
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

object UnmuteCommand {

    @Command(
        names = ["unmute"],
        description = "Unmute a player",
        permission = Permissions.MUTE_REMOVE,
        async = true
    )
    @JvmStatic
    fun execute(
        sender: CommandSender,
        @Flag(value = ["s", "silent"], description = "Silently unmute the player") silent: Boolean,
        @Param(name = "player") user: User,
        @Param(name = "reason", wildcard = true) reason: String
    ) {
        var issuer: UUID? = null
        if (sender is Player) {
            issuer = sender.uniqueId
        }

        val activePunishment = user.getActivePunishment(PunishmentType.MUTE)
        if (activePunishment == null) {
            sender.sendMessage("${user.getUsername()} ${ChatColor.RED}is not muted.")
            return
        }

        activePunishment.removedBy = issuer
        activePunishment.removedAt = System.currentTimeMillis()
        activePunishment.removalReason = reason

        EvilPunishments.instance.database.saveUser(user)

        val message = Message("PUNISHMENT_UPDATE", mapOf(
            "uuid" to user.uuid.toString(),
            "punishment" to activePunishment.uuid.toString(),
            "silent" to silent
        ))

        EvilPunishments.instance.pidgin.sendMessage(message)
    }

}