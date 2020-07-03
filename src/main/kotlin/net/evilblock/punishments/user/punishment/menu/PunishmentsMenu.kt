package net.evilblock.punishments.user.punishment.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.punishments.user.User
import net.evilblock.punishments.user.punishment.PunishmentType
import net.evilblock.punishments.user.punishment.menu.button.PunishmentButton
import org.bukkit.entity.Player

class PunishmentsMenu(private val user: User, private val punishmentType: PunishmentType) : PaginatedMenu() {

    override fun getPrePaginatedTitle(player: Player): String {
        return "${punishmentType.name.toLowerCase().capitalize()}s"
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        val punishments = user.getPunishments().filter { it.punishmentType == punishmentType }.sortedBy { -it.issuedAt }
        for (punishment in punishments) {
            buttons[buttons.size] = PunishmentButton(user, punishment)
        }

        return buttons
    }

    override fun onClose(player: Player, manualClose: Boolean) {
        if (manualClose) {
            Tasks.delayed(1L) {
                PunishmentTypesMenu(user).openMenu(player)
            }
        }
    }

}