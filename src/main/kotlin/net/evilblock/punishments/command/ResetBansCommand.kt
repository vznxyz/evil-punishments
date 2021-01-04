package net.evilblock.punishments.command

import net.evilblock.cubed.Cubed
import net.evilblock.cubed.command.Command
import net.evilblock.punishments.EvilPunishments
import net.evilblock.punishments.database.impl.MongoDatabase
import net.evilblock.punishments.user.User
import net.evilblock.punishments.user.UserHandler
import net.evilblock.punishments.user.punishment.Punishment
import net.evilblock.punishments.user.punishment.PunishmentType
import org.bson.Document
import org.bson.json.JsonMode
import org.bson.json.JsonWriterSettings
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender

object ResetBansCommand {

    private val JSON_WRITER_SETTINGS = JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build()


    @Command(
        names = ["punishments reset-bans"],
        description = "Resets all bans",
        permission = "op"
    )
    @JvmStatic
    fun execute(sender: CommandSender) {
        if (sender !is ConsoleCommandSender) {
            sender.sendMessage("${ChatColor.RED}That command must be executed through console!")
            return
        }

        if (EvilPunishments.instance.database is MongoDatabase) {
            for (document in EvilPunishments.instance.database.getCollection().find()) {
                try {
                    val removed = arrayListOf<Punishment>()

                    val user = Cubed.gson.fromJson(document.toJson(JSON_WRITER_SETTINGS), User::class.java)
                    for (punishment in user.getPunishments()) {
                        if (punishment.isActive() && punishment.punishmentType == PunishmentType.BAN) {
                            removed.add(punishment)
                        }
                    }

                    if (removed.isNotEmpty()) {
                        for (punishment in removed) {
                            punishment.removed = true
                            punishment.removedAt = System.currentTimeMillis()
                            punishment.removalReason = "Ban Wipe"
                        }

                        EvilPunishments.instance.database.saveUser(user)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        sender.sendMessage("done!")
    }

}