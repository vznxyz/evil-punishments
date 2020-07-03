package net.evilblock.punishments.database.impl

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.ReplaceOptions
import net.evilblock.cubed.Cubed
import net.evilblock.punishments.database.Database
import net.evilblock.punishments.database.result.IssuedByQueryResult
import net.evilblock.punishments.user.User
import net.evilblock.punishments.user.punishment.Punishment
import net.evilblock.punishments.user.punishment.PunishmentType
import org.bson.Document
import org.bson.json.JsonMode
import org.bson.json.JsonWriterSettings
import org.bukkit.Bukkit
import java.util.*

class MongoDatabase : Database {

    private val usersCollection: MongoCollection<Document> = Cubed.instance.mongo.client.getDatabase("wonder_punishments").getCollection("users")

    init {
        usersCollection.createIndex(Document("uuid", 1))
        usersCollection.createIndex(Document("ipAddress", 1))
    }

    override fun fetchUser(uuid: UUID, create: Boolean): User? {
        assert(!Bukkit.isPrimaryThread()) { "Cannot load user on primary thread" }

        val document = usersCollection.find(Document("uuid", uuid.toString())).first()
        if (document != null) {
            return Cubed.gson.fromJson(document.toJson(JSON_WRITER_SETTINGS), User::class.java)
        }

        return if (create) {
            User(uuid)
        } else {
            null
        }
    }

    override fun saveUser(user: User) {
        assert(!Bukkit.isPrimaryThread()) { "Cannot save user on primary thread" }

        val document = Document.parse(Cubed.gson.toJson(user))
        usersCollection.replaceOne(Document("uuid", user.uuid.toString()), document, ReplaceOptions().upsert(true))
    }

    override fun fetchActivePunishment(uuid: UUID, ipAddress: String?): Pair<UUID, Punishment>? {
        val orExpressions = listOf(
            Document("uuid", uuid.toString()),
            Document("ipAddresses", ipAddress)
        )

        for (matchingDocument in usersCollection.find(Document("\$or", orExpressions))) {
            val user = Cubed.gson.fromJson(matchingDocument.toJson(JSON_WRITER_SETTINGS), User::class.java)
            for (punishment in user.getPunishments()) {
                if (punishment.punishmentType == PunishmentType.BLACKLIST || punishment.punishmentType == PunishmentType.BAN) {
                    if (punishment.isActive()) {
                        return Pair(UUID.fromString(matchingDocument.getString("uuid")), punishment)
                    }
                }
            }
        }

        return null
    }

    override fun fetchAltAccounts(user: User): List<UUID> {
        val ipAddresses = user.getIpAddresses()
        if (ipAddresses.isEmpty()) {
            return emptyList()
        }

        val query = Document("ipAddresses", ipAddresses)
        val results = arrayListOf<UUID>()

        for (matchingDocument in usersCollection.find(query)) {
            val uuid = UUID.fromString(matchingDocument.getString("uuid"))
            if (uuid != null && !results.contains(uuid) && user.uuid != uuid) {
                results.add(uuid)
            }
        }

        return results
    }

    override fun fetchPunishmentsIssuedBy(uuid: UUID): List<IssuedByQueryResult> {
        val punishments = arrayListOf<IssuedByQueryResult>()

        val query = Document("punishments", Document("\$elemMatch", Document("issuedBy", uuid.toString())))
        for (matchingDocument in usersCollection.find(query)) {
            val user = Cubed.gson.fromJson(matchingDocument.toJson(JSON_WRITER_SETTINGS), User::class.java)
            for (punishment in user.getPunishments()) {
                if (punishment.issuedBy == uuid) {
                    punishments.add(IssuedByQueryResult(user, punishment))
                }
            }
        }

        return punishments
    }

    companion object {
        private val JSON_WRITER_SETTINGS = JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build()
    }

}