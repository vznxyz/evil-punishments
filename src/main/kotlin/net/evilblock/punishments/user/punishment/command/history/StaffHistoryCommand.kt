package net.evilblock.punishments.user.punishment.command.history

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import net.evilblock.punishments.EvilPunishments
import net.evilblock.punishments.user.User
import net.evilblock.punishments.user.punishment.menu.PunishmentsIssuedByMenu
import net.evilblock.punishments.util.Permissions
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object StaffHistoryCommand {

    @Command(
        names = ["staffhitory", "chistory"],
        description = "View punishments issued by a specific staff member",
        permission = Permissions.HISTORY_STAFF,
        async = true
    )
    @JvmStatic
    fun execute(player: Player, @Param(name = "player") user: User) {
        val punishments = EvilPunishments.instance.database.fetchPunishmentsIssuedBy(user.uuid)
        if (punishments.isEmpty()) {
            player.sendMessage("${ChatColor.RED}")
        } else {
            PunishmentsIssuedByMenu(user.uuid, punishments).openMenu(player)
        }
    }

}