package net.evilblock.punishments.user.command

import net.evilblock.cubed.Cubed
import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import net.evilblock.punishments.EvilPunishments
import net.evilblock.punishments.user.User
import net.evilblock.punishments.user.UserHandler
import net.evilblock.punishments.user.punishment.PunishmentType
import net.evilblock.punishments.util.Permissions
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object AltsCommand {

    @Command(
        names = ["alts", "findalts", "dupeip"],
        description = "Find a player's alt accounts",
        permission = Permissions.ALTS_VIEW,
        async = true
    )
    @JvmStatic
    fun execute(sender: CommandSender, @Param(name = "player") user: User) {
        var foundAlts = EvilPunishments.instance.database.fetchAltAccounts(user)

        if (sender is Player) {
            foundAlts = foundAlts.filter { it != sender.uniqueId }
        }

        val renderedNames = foundAlts.joinToString(separator = "${ChatColor.GRAY}, ") { altUuid ->
            val altUser = if (UserHandler.isUserLoaded(altUuid)) {
                UserHandler.getUser(altUuid)
            } else {
                EvilPunishments.instance.database.fetchUser(altUuid, false)
            }

            if (altUser != null) {
                if (altUser.getActivePunishment(PunishmentType.BAN) != null) {
                    return@joinToString "${ChatColor.RED}${altUser.getUsername()}"
                } else if (altUser.getActivePunishment(PunishmentType.BLACKLIST) != null) {
                    return@joinToString "${ChatColor.DARK_RED}${altUser.getUsername()}"
                }
            }

            Cubed.instance.uuidCache.name(altUuid)
        }

        sender.sendMessage("${ChatColor.YELLOW}Alts of ${user.getUsername()}: ${ChatColor.GRAY}$renderedNames")
    }

}