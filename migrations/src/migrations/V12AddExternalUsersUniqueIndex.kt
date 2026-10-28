package me.nekoalice.mafia.api.migrations.migrations

import org.jetbrains.exposed.v1.core.statements.StatementType
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction

object V12AddExternalUsersUniqueIndex : Migration {
    override val version = 12u

    context(transaction: R2dbcTransaction)
    override suspend fun up() {
        transaction.exec(
            """
                create unique index external_users_user_id_provider
                on external_users (user_id, provider)
            """.trimIndent(),
            explicitStatementType = StatementType.CREATE,
        )
    }

    context(transaction: R2dbcTransaction)
    override suspend fun down() {
        transaction.exec("drop index external_users_user_id_provider")
    }
}
