package net.evilblock.punishments.database.result

import net.evilblock.punishments.user.User
import net.evilblock.punishments.user.punishment.Punishment

class IssuedByQueryResult(val user: User, val punishment: Punishment)