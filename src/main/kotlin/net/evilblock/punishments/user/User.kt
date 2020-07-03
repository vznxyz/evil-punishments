package net.evilblock.punishments.user

import net.evilblock.cubed.Cubed
import net.evilblock.punishments.user.punishment.Punishment
import net.evilblock.punishments.user.punishment.PunishmentType
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class User(val uuid: UUID) {

    private var punishments: MutableList<Punishment> = ArrayList()
    private var ipAddresses: MutableSet<String> = HashSet()

    /**
     * Returns the user's username.
     */
    fun getUsername(): String {
        return Cubed.instance.uuidCache.name(uuid)
    }

    /**
     * Returns a copy of the user's [punishments].
     */
    fun getPunishments(): List<Punishment> {
        return punishments.toList()
    }

    /**
     * Returns the first punishment that matches the given [type] and is also active.
     */
    fun getActivePunishment(type: PunishmentType): Punishment? {
        for (punishment in punishments) {
            if (punishment.punishmentType == type && punishment.isActive()) {
                return punishment
            }
        }
        return null
    }

    /**
     * Adds the given [punishment] to the user's [punishments].
     */
    fun addPunishment(punishment: Punishment) {
        punishments.add(punishment)
    }

    /**
     * Returns a list of the user's [ipAddresses].
     */
    fun getIpAddresses(): List<String> {
        return ipAddresses.toList()
    }

    /**
     * Adds the given [address] to the user's [ipAddresses].
     */
    fun addIpAddress(address: String) {
        ipAddresses.add(address)
    }

}