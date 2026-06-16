package me.nekoalice.mafia.api.server.storage.pg

import me.nekoalice.mafia.api.server.storage.base.StorageProvider

class PostgreSQLStorageProvider(
    override val game: PostgreSQLGameStorage,
    override val player: PostgreSQLPlayerStorage,
    override val tournament: PostgreSQLTournamentStorage
) : StorageProvider {
    override suspend fun ping() = readonlyTx { exec("select 1") }
}
