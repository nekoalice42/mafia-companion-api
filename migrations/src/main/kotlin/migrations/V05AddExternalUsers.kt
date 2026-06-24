package me.nekoalice.mafia.api.migrations.migrations

import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils

object V05AddExternalUsers : Migration {
    override val version: UInt = 5u

    context(transaction: R2dbcTransaction)
    override suspend fun up() {
        SchemaUtils.create(Tables.ExternalUsers)
        // language="SQL"
        transaction.execInBatch(
            listOf(
                "alter table users alter column password_hash drop not null",
            )
        )
    }

    context(transaction: R2dbcTransaction)
    override suspend fun down() {
        SchemaUtils.drop(Tables.ExternalUsers)
        // language="SQL"
        transaction.execInBatch(
            listOf(
                "delete from users where password_hash is null",
                "alter table users alter column password_hash set not null",
            )
        )
    }

    @Suppress("unused")
    private object Tables {
        private object Users : UuidTable("users", uuidVersion = UuidVersion.V7)

        object ExternalUsers : CompositeIdTable("external_users") {
            val externalId = text("external_id").entityId()
            val provider = text("provider").entityId()

            val userId = reference("user_id", Users)

            override val primaryKey: PrimaryKey = PrimaryKey(externalId, provider)
        }
    }
}
