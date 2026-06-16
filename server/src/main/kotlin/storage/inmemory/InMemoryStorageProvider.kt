package me.nekoalice.mafia.api.server.storage.inmemory

import me.nekoalice.mafia.api.server.storage.base.StorageProvider

class InMemoryStorageProvider(
    override val game: InMemoryGameStorage,
    override val player: InMemoryPlayerStorage,
    override val tournament: InMemoryTournamentStorage
) : StorageProvider {
    override suspend fun ping() = Unit
}
