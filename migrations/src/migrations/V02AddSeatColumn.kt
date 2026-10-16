package me.nekoalice.mafia.api.migrations.migrations

import org.jetbrains.exposed.v1.core.statements.StatementType
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction

object V02AddSeatColumn : Migration {
    override val version = 2u

    context(transaction: R2dbcTransaction)
    override suspend fun up() {
        transaction.exec(
            "alter table in_game_players add column seat integer null check (seat >= 1 and seat <= 10)",
            explicitStatementType = StatementType.ALTER,
        )
        transaction.exec(
            """
                with cte as (
                    select player_id, game_id, row_number() over ( partition by game_id order by ctid ) as seat
                    from in_game_players
                )
                update in_game_players p 
                set seat = cte.seat
                from cte
                where p.player_id = cte.player_id
                    and p.game_id = cte.game_id
                    and p.seat is null
            """.trimIndent(),
            explicitStatementType = StatementType.UPDATE,
        )
        transaction.exec(
            "alter table in_game_players alter column seat set not null",
            explicitStatementType = StatementType.ALTER,
        )
    }

    context(transaction: R2dbcTransaction)
    override suspend fun down() {
        transaction.exec("alter table in_game_players drop column seat")
    }
}
