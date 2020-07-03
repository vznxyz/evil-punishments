package net.evilblock.punishments.user.punishment

import net.evilblock.cubed.util.TimeUtil
import net.evilblock.punishments.util.Permissions
import java.util.*

class Punishment(val uuid: UUID = UUID.randomUUID(), val punishmentType: PunishmentType) {

    var reason: String = ""
    var issuedBy: UUID? = null
    val issuedAt: Long = System.currentTimeMillis()
    var expiresAt: Long? = null

    var removalReason: String? = null
    var removedBy: UUID? = null
    var removedAt: Long? = null

    fun isActive(): Boolean {
        return removedAt == null && (expiresAt == null || System.currentTimeMillis() < expiresAt!!)
    }

    fun isPermanent(): Boolean {
        return expiresAt == null
    }

    fun getDuration(): Long {
        return expiresAt!! - issuedAt
    }

    fun getFormattedDuration(): String {
        return TimeUtil.formatIntoDetailedString((getDuration() / 1000.0).toInt())
    }

    fun getRemainingTime(): Long {
        return expiresAt!! - System.currentTimeMillis()
    }

    fun getFormattedRemainingTime(): String {
        return TimeUtil.formatIntoDetailedString((getRemainingTime() / 1000.0).toInt())
    }

    fun getViewPermission(): String {
        return when (punishmentType) {
            PunishmentType.BLACKLIST -> Permissions.BLACKLIST_VIEW
            PunishmentType.BAN -> Permissions.BAN_VIEW
            PunishmentType.MUTE -> Permissions.MUTE_VIEW
            PunishmentType.WARN -> Permissions.WARN_VIEW
        }
    }

    fun getDeletePermission(): String {
        return when (punishmentType) {
            PunishmentType.BLACKLIST -> "op"
            PunishmentType.BAN -> Permissions.BAN_REMOVE
            PunishmentType.MUTE -> Permissions.MUTE_REMOVE
            PunishmentType.WARN -> Permissions.WARN_REMOVE
        }
    }

}