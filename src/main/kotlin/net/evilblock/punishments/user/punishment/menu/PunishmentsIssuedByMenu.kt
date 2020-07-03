package net.evilblock.punishments.user.punishment.menu

import net.evilblock.cubed.Cubed
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.punishments.database.result.IssuedByQueryResult
import net.evilblock.punishments.user.punishment.menu.button.PunishmentButton
import org.bukkit.entity.Player
import java.util.*

class PunishmentsIssuedByMenu(private val issuedBy: UUID, private val punishments: List<IssuedByQueryResult>) : PaginatedMenu() {

    override fun getPrePaginatedTitle(player: Player): String {
        return "Staff History of ${Cubed.instance.uuidCache.name(issuedBy)}"
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        for (queryResult in punishments.sortedBy { -it.punishment.issuedAt }) {
            buttons[buttons.size] = PunishmentButton(queryResult.user, queryResult.punishment, true)
        }

        return buttons
    }

}