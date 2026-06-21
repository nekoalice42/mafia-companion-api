package me.nekoalice.mafia.api.migrations.migrations

import com.password4j.Password
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.insert
import kotlin.uuid.Uuid

object V04AddTokensAndPlayers : Migration {
    override val version: UInt = 4u

    context(transaction: R2dbcTransaction)
    override suspend fun up() {
        SchemaUtils.create(*Tables.allTables)
        val passwordHash = hashPassword("D3faultPassw0rd!")
        Tables.Users.insert {
            it[Tables.Users.id] = Uuid.parse("019ee53f-bc60-7000-8000-000000000000")
            it[Tables.Users.username] = "admin"
            it[Tables.Users.passwordHash] = passwordHash
        }
    }

    context(transaction: R2dbcTransaction)
    override suspend fun down() {
        transaction.execInBatch(
            Tables.allTables.reversed().flatMap { it.dropStatement() },
        )
    }

    @Suppress("unused")
    private object Tables {
        object Users : UuidTable("users", uuidVersion = UuidVersion.V7) {
            val username = text("username").uniqueIndex()
            val passwordHash = text("password_hash")
        }

        object AccessTokens : IdTable<Uuid>("access_tokens") {
            override val id = uuid("user_id").references(Users.id).entityId()
            val hash = text("hash", eagerLoading = true)
            val expiresAt = timestampWithTimeZone("expires_at")

            override val primaryKey = PrimaryKey(id)
        }

        object RefreshTokens : IdTable<Uuid>("refresh_tokens") {
            override val id = uuid("user_id").references(Users.id).entityId()
            val hash = text("hash", eagerLoading = true)
            val expiresAt = timestampWithTimeZone("expires_at")

            override val primaryKey = PrimaryKey(id)
        }

        val allTables = arrayOf(Users, AccessTokens, RefreshTokens)
    }

    private suspend fun hashPassword(password: String): String = withContext(Dispatchers.Default) {
        Password.hash(password)
            .addRandomSalt()
            .withArgon2()
            .result
    }
}
