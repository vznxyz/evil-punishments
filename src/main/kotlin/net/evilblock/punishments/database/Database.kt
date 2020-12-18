package net.evilblock.punishments.database

import net.evilblock.punishments.database.result.IssuedByQueryResult
import net.evilblock.punishments.user.User
import net.evilblock.punishments.user.punishment.Punishment
import java.util.*

interface Database {

    /**
     * Fetches a [User] by the given [uuid].
     */
    fun fetchUser(uuid: UUID, create: Boolean): User?

    /**
     * Saves a given [user].
     */
    fun saveUser(user: User)

    /**
     * Fetches an active [Punishment] linked to the given [uuid] and [ipAddress].
     */
    fun fetchActivePunishment(uuid: UUID, ipAddress: String?): Pair<UUID, Punishment>?

    /**
     * Fetches a list of player UUIDs linked to the given [user].
     */
    fun fetchAltAccounts(user: User): List<UUID>

    /**
     * Fetches a list of punishments issued by the given [uuid].
     */
    fun fetchPunishmentsIssuedBy(uuid: UUID): List<IssuedByQueryResult>

    fun performMigrations() {

    }

}