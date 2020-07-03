package net.evilblock.punishments.user.punishment

import org.bukkit.ChatColor

enum class PunishmentType constructor(
    val action: String,
    val color: ChatColor,
    var kick: Boolean,
    vararg kickMessages: String
) {

    BLACKLIST("blacklisted", ChatColor.DARK_RED, true, "&cYou've been blacklisted from MineJunkie."),
    BAN("banned", ChatColor.RED, true, "&cYou've been suspended from MineJunkie."),
    MUTE("muted", ChatColor.YELLOW, false),
    WARN("warned", ChatColor.GREEN, false);

    val kickMessages: List<String> = listOf(*kickMessages)

}
