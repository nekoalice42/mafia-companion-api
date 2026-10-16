package me.nekoalice.mafia.api.migrations.migrations

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils

object V06AddAuthCodes : Migration {
    override val version: UInt = 6u

    context(transaction: R2dbcTransaction)
    override suspend fun up() {
        SchemaUtils.create(*Tables.allTables)
    }

    context(transaction: R2dbcTransaction)
    override suspend fun down() {
        SchemaUtils.drop(*Tables.allTables)
    }

    @Suppress("unused")
    private object Tables {
        private object Users : UuidTable("users", uuidVersion = UuidVersion.V7)

        object AuthCodes : Table("auth_codes") {
            val code = text("code")
            val userId = reference("user_id", Users)
            val expiresAt = timestampWithTimeZone("expires_at")

            override val primaryKey = PrimaryKey(code)
        }

        object ClientStates : Table("client_states") {
            val state = text("state")
            val redirectUrl = text("redirect_url").nullable()
            val clientState = text("client_state").nullable()
            val expiresAt = timestampWithTimeZone("expires_at")

            override val primaryKey = PrimaryKey(state)
        }

        val allTables = arrayOf(AuthCodes, ClientStates)
    }
}
