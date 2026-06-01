package me.nekoalice.mafia.api.server.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import me.nekoalice.mafia.api.dto.models.Tournament
import me.nekoalice.mafia.api.dto.models.TournamentId
import me.nekoalice.mafia.api.server.storage.base.TournamentStorage

class InMemoryTournamentStorage : TournamentStorage {
    private val tournaments = mutableMapOf<TournamentId, Tournament>()

    override suspend fun add(tournament: Tournament) {
        tournaments[tournament.id] = tournament
    }

    override suspend fun edit(id: TournamentId, tournament: Tournament) {
        tournaments[id] = tournament
    }

    override suspend fun getByIdOrNull(id: TournamentId): Tournament? = tournaments[id]

    override fun getAll(): Flow<Tournament> = tournaments.values.asFlow()
}
