package me.nekoalice.mafia.api.migrations.migrations

import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction

object V08NullableWinnerTeam : Migration {
    override val version: UInt = 8u

    context(transaction: R2dbcTransaction)
    override suspend fun up() {
        transaction.exec("alter table games alter column winner_team drop not null")
    }

    context(transaction: R2dbcTransaction)
    override suspend fun down() {
        transaction.exec("alter table games alter column winner_team set not null")
    }
}
