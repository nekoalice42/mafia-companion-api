package me.nekoalice.mafia.api.server.storage.pg

import me.nekoalice.mafia.api.server.storage.base.AuthStorage
import me.nekoalice.mafia.api.server.storage.base.StorageProvider
import me.nekoalice.mafia.api.server.storage.base.UserStorage

class PostgreSQLStorageProvider(
    override val auth: AuthStorage,
    override val game: PostgreSQLGameStorage,
    override val player: PostgreSQLPlayerStorage,
    override val tournament: PostgreSQLTournamentStorage,
    override val user: UserStorage,
) : StorageProvider {
    override suspend fun ping() = readonlyTx { exec("select 1") }
}
