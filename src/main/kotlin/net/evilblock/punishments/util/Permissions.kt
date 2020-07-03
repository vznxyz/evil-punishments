package net.evilblock.punishments.util

object Permissions {

    const val PROTECTED = "punishments.protected" // User is protected from punishments

    const val HISTORY = "punishments.history"
    const val HISTORY_STAFF = "punishments.history.staff"

    const val BLACKLIST = "punishments.blacklist.create"
    const val BLACKLIST_REMOVE = "punishments.blacklist.remove"
    const val BLACKLIST_VIEW = "punishments.blacklist.view"

    const val BAN = "punishments.ban.create"
    const val BAN_PERMANENT = "punishments.ban.create.permanent"
    const val BAN_REMOVE = "punishments.ban.remove"
    const val BAN_VIEW = "punishments.ban.view"

    const val MUTE = "punishments.mute.create"
    const val MUTE_PERMANENT = "punishments.mute.create.permanent"
    const val MUTE_REMOVE = "punishments.mute.remove"
    const val MUTE_VIEW = "punishments.mute.view"

    const val WARN = "punishments.warn.create"
    const val WARN_REMOVE = "punishments.warn.remove"
    const val WARN_VIEW = "punishments.warn.view"

}