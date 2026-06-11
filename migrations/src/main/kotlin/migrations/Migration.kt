package me.nekoalice.mafia.api.migrations.migrations

interface Migration {
    val version: Int
    suspend fun up()
    suspend fun down()
}
