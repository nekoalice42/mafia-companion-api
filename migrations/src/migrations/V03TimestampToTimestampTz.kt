package me.nekoalice.mafia.api.migrations.migrations

import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction

object V03TimestampToTimestampTz : Migration {
    override val version: UInt = 3u

    context(transaction: R2dbcTransaction)
    override suspend fun up() {
        transaction.execInBatch(
            listOf(
                getStmt("games", "started_at", "timestamptz"),
                getStmt("tournaments", "starts_at", "timestamptz"),
                getStmt("tournaments", "ends_at", "timestamptz"),
            )
        )
    }

    context(transaction: R2dbcTransaction)
    override suspend fun down() {
        transaction.execInBatch(
            listOf(
                getStmt("games", "started_at", "timestamp"),
                getStmt("tournaments", "starts_at", "timestamp"),
                getStmt("tournaments", "ends_at", "timestamp"),
            )
        )
    }

    private fun getStmt(table: String, column: String, type: String) =
        """
            ALTER TABLE "$table"
            ALTER COLUMN "$column"
            TYPE "$type"
            USING "$column"::"$type"
        """.trimIndent()
}
