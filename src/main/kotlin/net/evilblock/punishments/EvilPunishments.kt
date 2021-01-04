package net.evilblock.punishments

import net.evilblock.cubed.Cubed
import net.evilblock.cubed.CubedOptions
import net.evilblock.cubed.command.CommandHandler
import net.evilblock.pidgin.Pidgin
import net.evilblock.pidgin.PidginOptions
import net.evilblock.punishments.command.MigrateCommand
import net.evilblock.punishments.command.ResetBansCommand
import net.evilblock.punishments.database.impl.MongoDatabase
import net.evilblock.punishments.user.User
import net.evilblock.punishments.user.command.AltsCommand
import net.evilblock.punishments.user.command.parameter.UserParameterType
import net.evilblock.punishments.user.listener.UserListeners
import net.evilblock.punishments.user.message.UserMessageListeners
import net.evilblock.punishments.user.punishment.command.ban.BanCommand
import net.evilblock.punishments.user.punishment.command.blacklist.BlacklistCommand
import net.evilblock.punishments.user.punishment.command.mute.MuteCommand
import net.evilblock.punishments.user.punishment.command.ban.TempBanCommand
import net.evilblock.punishments.user.punishment.command.mute.TempMuteCommand
import net.evilblock.punishments.user.punishment.command.ban.UnbanCommand
import net.evilblock.punishments.user.punishment.command.blacklist.UnblacklistCommand
import net.evilblock.punishments.user.punishment.command.mute.UnmuteCommand
import net.evilblock.punishments.user.punishment.command.history.CheckCommand
import net.evilblock.punishments.user.punishment.command.history.StaffHistoryCommand
import net.evilblock.punishments.user.punishment.command.warn.WarnCommand
import org.bukkit.plugin.java.JavaPlugin

class EvilPunishments : JavaPlugin() {

    lateinit var database: MongoDatabase
    lateinit var pidgin: Pidgin

    override fun onEnable() {
        instance = this

        saveDefaultConfig()

        Cubed.instance.configureOptions(CubedOptions(requireRedis = true, requireMongo = true))

        database = MongoDatabase()

        pidgin = Pidgin("EVIL_PUNISHMENTS", Cubed.instance.redis.jedisPool!!, PidginOptions(async = true))
        pidgin.registerListener(UserMessageListeners)

        loadListeners()
        loadCommands()
    }

    private fun loadListeners() {
        server.pluginManager.registerEvents(UserListeners, this)
    }

    private fun loadCommands() {
        CommandHandler.registerParameterType(User::class.java, UserParameterType())

        CommandHandler.registerClass(MigrateCommand.javaClass)
        CommandHandler.registerClass(ResetBansCommand.javaClass)

        CommandHandler.registerClass(BlacklistCommand.javaClass)
        CommandHandler.registerClass(UnblacklistCommand.javaClass)

        CommandHandler.registerClass(BanCommand.javaClass)
        CommandHandler.registerClass(TempBanCommand.javaClass)
        CommandHandler.registerClass(UnbanCommand.javaClass)

        CommandHandler.registerClass(MuteCommand.javaClass)
        CommandHandler.registerClass(TempMuteCommand.javaClass)
        CommandHandler.registerClass(UnmuteCommand.javaClass)

        CommandHandler.registerClass(WarnCommand.javaClass)

        CommandHandler.registerClass(AltsCommand.javaClass)

        CommandHandler.registerClass(CheckCommand.javaClass)
        CommandHandler.registerClass(StaffHistoryCommand.javaClass)
    }

    companion object {
        @JvmStatic
        lateinit var instance: EvilPunishments
    }

}