package me.nekoalice.mafia.api.migrations.migrations

import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction

interface Migration {
    val version: UInt
    context(transaction: R2dbcTransaction)
    suspend fun up()
    context(transaction: R2dbcTransaction)
    suspend fun down()
}
