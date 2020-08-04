package net.evilblock.punishments.user.command

import net.evilblock.cubed.Cubed
import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import net.evilblock.cubed.util.TextSplitter
import net.evilblock.punishments.EvilPunishments
import net.evilblock.punishments.user.User
import net.evilblock.punishments.user.UserHandler
import net.evilblock.punishments.user.punishment.PunishmentType
import net.evilblock.punishments.util.Permissions
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

object AltsCommand {

    @Command(
        names = ["alts", "findalts", "dupeip"],
        description = "Find a player's alt accounts",
        permission = Permissions.ALTS_VIEW,
        async = true
    )
    @JvmStatic
    fun execute(sender: CommandSender, @Param(name = "player") user: User) {
        val foundAlts = EvilPunishments.instance.database.fetchAltAccounts(user).filter { it != user.uuid }
        if (foundAlts.isEmpty()) {
            sender.sendMessage("${ChatColor.RED}No alts found for ${user.getUsername()}!")
            return
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

        if (renderedNames.length < 4000) {
            sender.sendMessage("${ChatColor.YELLOW}Alts of ${user.getUsername()} ${ChatColor.BOLD}(${foundAlts.size}): ${ChatColor.GRAY}$renderedNames")
        } else {
            var first = true
            for (message in TextSplitter.split(length = 4000, text = renderedNames)) {
                if (first) {
                    sender.sendMessage("${ChatColor.YELLOW}Alts of ${user.getUsername()} ${ChatColor.BOLD}(${foundAlts.size}): ${ChatColor.GRAY}$message")
                } else {
                    sender.sendMessage("${ChatColor.GRAY}$message")
                }

                first = false
            }
        }
    }

}