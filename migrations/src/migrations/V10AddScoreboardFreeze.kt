package me.nekoalice.mafia.api.migrations.migrations

import org.jetbrains.exposed.v1.core.statements.StatementType
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction

object V10AddScoreboardFreeze : Migration {
    override val version = 10u

    context(transaction: R2dbcTransaction)
    override suspend fun up() {
        transaction.exec(
            """
                alter table tournaments
                add column scoreboard_freezes_at timestamp with time zone null
                check (
                    scoreboard_freezes_at is null
                    or ends_at is null
                    or scoreboard_freezes_at <= ends_at
                )
            """.trimIndent(),
            explicitStatementType = StatementType.ALTER,
        )
    }

    context(transaction: R2dbcTransaction)
    override suspend fun down() {
        transaction.exec(
            "alter table tournaments drop column scoreboard_freezes_at",
            explicitStatementType = StatementType.ALTER,
        )
    }
}
