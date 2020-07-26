package net.evilblock.punishments.user.command.parameter

import net.evilblock.cubed.Cubed
import net.evilblock.cubed.command.data.parameter.ParameterType
import net.evilblock.cubed.store.bukkit.UUIDCache
import net.evilblock.punishments.EvilPunishments
import net.evilblock.punishments.user.User
import net.evilblock.punishments.user.UserHandler
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class UserParameterType : ParameterType<User?> {

    override fun transform(sender: CommandSender, source: String): User? {
        if (source == "self") {
            if (sender is Player) {
                return UserHandler.getUser(sender.uniqueId)
            } else {
                throw IllegalStateException("Can't transform console sender to User")
            }
        }

        var uuid: UUID? = try {
            UUID.fromString(source)
        } catch (e: Exception) {
            Cubed.instance.uuidCache.uuid(source)
        }

        if (uuid == null) {
            val optionalProfile = UUIDCache.fetchFromMojang(source)
            if (optionalProfile.isPresent) {
                uuid = optionalProfile.get().first
            }

            return if (uuid == null) {
                sender.sendMessage("${ChatColor.RED}Couldn't find a player by the name or ID '$source'1.")
                null
            } else {
                EvilPunishments.instance.database.fetchUser(uuid, true)
            }
        }

        val user = EvilPunishments.instance.database.fetchUser(uuid, true)
        if (user == null) {
            sender.sendMessage("${ChatColor.RED}Couldn't find a player by the name or ID '$source'2.")
        }

        return user
    }

    override fun tabComplete(player: Player, flags: Set<String>, source: String): List<String> {
        val completions = arrayListOf<String>()

        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.name.toLowerCase().startsWith(source.toLowerCase())) {
                completions.add(onlinePlayer.name)
            }
        }

        return completions
    }

}