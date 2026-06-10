package me.nekoalice.mafia.api.server.storage.inmemory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import me.nekoalice.mafia.api.dto.models.Tournament
import me.nekoalice.mafia.api.dto.models.TournamentId
import me.nekoalice.mafia.api.server.storage.base.TournamentStorage

class InMemoryTournamentStorage : TournamentStorage {
    private val tournaments = mutableMapOf<TournamentId, Tournament>()

    override suspend fun editOrAdd(id: TournamentId, item: Tournament) {
        tournaments[id] = item
    }

    override suspend fun getByIdOrNull(id: TournamentId): Tournament? = tournaments[id]

    override suspend fun getAll(): List<Tournament> = tournaments.values.toList()

    override suspend fun delete(id: TournamentId) {
        tournaments.remove(id)
    }
}
