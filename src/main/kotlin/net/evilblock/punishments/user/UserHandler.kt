package net.evilblock.punishments.user

import org.bukkit.Bukkit
import java.util.*

object UserHandler {

    private val loadedUsers = HashMap<UUID, User>()

    fun getLoadedUsers(): List<User> {
        return loadedUsers.values.toList()
    }

    fun isUserLoaded(uuid: UUID): Boolean {
        return loadedUsers.containsKey(uuid)
    }

    fun getUser(uuid: UUID): User? {
        return loadedUsers[uuid]
    }

    fun cacheUser(user: User) {
        loadedUsers[user.uuid] = user
    }

    fun forgetUser(user: User) {
        loadedUsers.remove(user.uuid)
    }

    fun canPunish(executor: User, punished: User): Boolean {
        val executorPlayer = Bukkit.getOfflinePlayer(executor.uuid)
        val punishedPlayer = Bukkit.getOfflinePlayer(punished.uuid)

        if (executorPlayer != null && punishedPlayer != null) {
            if (punishedPlayer.isOp && !executorPlayer.isOp) {
                return false
            }
        }

        return true
    }

}