package net.evilblock.punishments.user.punishment.menu.button

import net.evilblock.cubed.Cubed
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.TimeUtil
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.EzPrompt
import net.evilblock.pidgin.message.Message
import net.evilblock.punishments.EvilPunishments
import net.evilblock.punishments.user.User
import net.evilblock.punishments.user.punishment.Punishment
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import java.util.*

class PunishmentButton(
    private val user: User,
    private val punishment: Punishment,
    private val appendIssuedTo: Boolean = false
) : Button() {

    override fun getName(player: Player): String {
        return "${punishment.punishmentType.color}${ChatColor.BOLD}${punishment.punishmentType.name}"
    }

    override fun getDescription(player: Player): List<String> {
        val description = arrayListOf<String>()

        val addedBy = if (punishment.issuedBy == null) {
            "Console"
        } else {
            Cubed.instance.uuidCache.name(punishment.issuedBy!!)
        }

        description.add(BAR)

        if (appendIssuedTo) {
            description.add("${ChatColor.YELLOW}Issued to: ${ChatColor.RED}${user.getUsername()}")
        }

        description.add("${ChatColor.YELLOW}Issued by: ${ChatColor.RED}$addedBy")
        description.add("${ChatColor.YELLOW}Issued at: ${ChatColor.RED}${TimeUtil.formatIntoCalendarString(Date(punishment.issuedAt))}")
        description.add("${ChatColor.YELLOW}Reason: ${ChatColor.RED}${ChatColor.ITALIC}${punishment.reason}")

        if (punishment.isActive()) {
            if (punishment.expiresAt == null) {
                description.add("${ChatColor.YELLOW}Duration: ${ChatColor.RED}Permanent")
            } else {
                val timeRemaining = TimeUtil.formatIntoDetailedString(((punishment.expiresAt!! - System.currentTimeMillis()) / 1_000L).toInt())
                description.add("${ChatColor.YELLOW}Time remaining: ${ChatColor.RED}$timeRemaining")
            }

            if (player.hasPermission(punishment.getDeletePermission())) {
                description.add(BAR)
                description.add("${ChatColor.YELLOW}Click to remove this punishment.")
            }
        } else if (punishment.removedAt != null) {
            val removedBy = if (punishment.removedBy == null) {
                "Console"
            } else {
                Cubed.instance.uuidCache.name(punishment.removedBy!!)
            }

            val removedAt = TimeUtil.formatIntoCalendarString(Date(punishment.removedAt!!))

            description.add(BAR)
            description.add("${ChatColor.YELLOW}Removed by: ${ChatColor.RED}$removedBy")
            description.add("${ChatColor.YELLOW}Removed at: ${ChatColor.RED}$removedAt")
            description.add("${ChatColor.YELLOW}Reason: ${ChatColor.RED}${ChatColor.ITALIC}${punishment.removalReason}")
        }

        description.add(BAR)

        return description
    }

    override fun getMaterial(player: Player): Material {
        return Material.INK_SACK
    }

    override fun getDamageValue(player: Player): Byte {
        return if (punishment.isActive()) {
            10
        } else {
            1
        }
    }

    override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
        if (punishment.removedAt == null) {
            EzPrompt.Builder()
                .promptText("${ChatColor.GREEN}Please specify a valid reason.")
                .acceptInput { player, input ->
                    if (input.equals("cancel", ignoreCase = true)) {
                        player.sendMessage("${ChatColor.YELLOW}Cancelled procedure.")
                        return@acceptInput
                    }

                    punishment.removedBy = player.uniqueId
                    punishment.removedAt = System.currentTimeMillis()
                    punishment.removalReason = input

                    Tasks.async {
                        EvilPunishments.instance.database.saveUser(user)

                        val message = Message("PUNISHMENT_UPDATE", mapOf(
                            "uuid" to user.uuid.toString(),
                            "punishment" to punishment.uuid.toString(),
                            "silent" to true
                        ))

                        EvilPunishments.instance.pidgin.sendMessage(message)
                    }

                    player.sendMessage("${ChatColor.GOLD}Punishment removed.")
                }
                .build()
                .start(player)
        }
    }

}