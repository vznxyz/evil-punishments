package net.evilblock.punishments.user.punishment.command.mute

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.flag.Flag
import net.evilblock.cubed.command.data.parameter.Param
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

object MuteCommand {

    @Command(
        names = ["mute"],
        description = "Mute a player",
        permission = Permissions.MUTE_PERMANENT,
        async = true
    )
    @JvmStatic
    fun execute(
        sender: CommandSender,
        @Flag(value = ["s", "silent"], description = "Silently mute the player") silent: Boolean,
        @Param(name = "player") user: User,
        @Param(name = "reason", wildcard = true) reason: String
    ) {
        var issuer: UUID? = null
        if (sender is Player) {
            issuer = sender.uniqueId
        }

        if (user.getActivePunishment(PunishmentType.MUTE) != null) {
            sender.sendMessage("${user.getUsername()} ${ChatColor.RED}is already muted!")
            return
        }

        val punishment = Punishment(uuid = UUID.randomUUID(), punishmentType = PunishmentType.MUTE)
        punishment.reason = reason
        punishment.issuedBy = issuer

        user.addPunishment(punishment)
        EvilPunishments.instance.database.saveUser(user)

        val message = Message("PUNISHMENT_UPDATE", mapOf(
            "uuid" to user.uuid.toString(),
            "punishment" to punishment.uuid.toString(),
            "silent" to silent)
        )

        EvilPunishments.instance.pidgin.sendMessage(message)
    }
}