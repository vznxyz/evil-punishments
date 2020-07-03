package net.evilblock.punishments.user.punishment.command.history

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import net.evilblock.punishments.user.User
import net.evilblock.punishments.util.Permissions
import net.evilblock.punishments.user.punishment.menu.PunishmentTypesMenu
import org.bukkit.entity.Player

object CheckCommand {

    @Command(
        names = ["check", "c", "history"],
        description = "View a player's punishments",
        permission = Permissions.HISTORY,
        async = true
    )
    @JvmStatic
    fun execute(player: Player, @Param(name = "player") user: User) {
        PunishmentTypesMenu(user).openMenu(player)
    }

}