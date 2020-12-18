package net.evilblock.punishments.command

import net.evilblock.cubed.command.Command
import net.evilblock.punishments.EvilPunishments
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender

object MigrateCommand {

    @Command(
        names = ["punishments migrate"],
        description = "Perform migrations on punishments DB",
        permission = "op",
        async = true
    )
    @JvmStatic
    fun execute(sender: CommandSender) {
        if (sender !is ConsoleCommandSender) {
            sender.sendMessage("${ChatColor.RED}This command must be executed through console!")
            return
        }

        EvilPunishments.instance.database.performMigrations()
    }

}