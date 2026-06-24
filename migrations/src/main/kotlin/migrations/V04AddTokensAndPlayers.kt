package me.nekoalice.mafia.api.migrations.migrations

import com.password4j.Argon2Function
import com.password4j.Password
import com.password4j.types.Argon2
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
        Tables.Users.insert {
            it[Tables.Users.id] = Uuid.parse("019ee53f-bc60-7000-8000-000000000000")
            it[Tables.Users.username] = "admin"
            it[Tables.Users.passwordHash] = hashPassword("D3faultPassw0rd!")
        }
    }

    context(transaction: R2dbcTransaction)
    override suspend fun down() {
        SchemaUtils.drop(*Tables.allTables)
    }

    @Suppress("unused")
    private object Tables {
        object Users : UuidTable("users", uuidVersion = UuidVersion.V7) {
            val username = text("username").uniqueIndex()
            val passwordHash = text("password_hash")
        }

        object AccessTokens : IdTable<Uuid>("access_tokens") {
            override val id = uuid("user_id").references(Users.id).entityId()
            val hash = text("hash")
            val expiresAt = timestampWithTimeZone("expires_at")

            override val primaryKey = PrimaryKey(id)
        }

        object RefreshTokens : IdTable<Uuid>("refresh_tokens") {
            override val id = uuid("user_id").references(Users.id).entityId()
            val hash = text("hash")
            val expiresAt = timestampWithTimeZone("expires_at")

            override val primaryKey = PrimaryKey(id)
        }

        val allTables = arrayOf(Users, AccessTokens, RefreshTokens)
    }

    private val argon2Hasher = Argon2Function.getInstance(
        15360,
        2,
        1,
        32,
        Argon2.ID,
        19,
    )

    @Suppress("SameParameterValue")
    private fun hashPassword(password: String): String =
        Password.hash(password)
            .addRandomSalt(64)
            .with(argon2Hasher)
            .result
}
