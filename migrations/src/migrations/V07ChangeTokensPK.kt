package me.nekoalice.mafia.api.migrations.migrations

import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction

object V07ChangeTokensPK : Migration {
    override val version: UInt = 7u

    context(transaction: R2dbcTransaction)
    override suspend fun up() {
        transaction.execInBatch(
            listOf(
                getDropSql("access_tokens"),
                getDropSql("refresh_tokens"),
                getAddSql("access_tokens", "hash"),
                getAddSql("refresh_tokens", "hash"),
            ),
        )
    }

    context(transaction: R2dbcTransaction)
    override suspend fun down() {
        transaction.execInBatch(
            listOf(
                getDropSql("access_tokens"),
                getDropSql("refresh_tokens"),
                getAddSql("access_tokens", "user_id"),
                getAddSql("refresh_tokens", "user_id"),
            ),
        )
    }

    @Language("sql")
    private fun getDropSql(tableName: String): String =
        """
            alter table "$tableName"
            drop constraint "${tableName}_pkey"
        """.trimIndent()

    @Language("sql")
    private fun getAddSql(tableName: String, pkeyColumnName: String): String =
        """
            alter table "$tableName"
            add constraint "${tableName}_pkey"
            primary key ("$pkeyColumnName")
        """.trimIndent()
}
