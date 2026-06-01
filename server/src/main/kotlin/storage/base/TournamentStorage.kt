package me.nekoalice.mafia.api.server.storage.base

import kotlinx.coroutines.flow.Flow
import me.nekoalice.mafia.api.dto.models.Tournament
import me.nekoalice.mafia.api.dto.models.TournamentId

interface TournamentStorage {
    suspend fun add(tournament: Tournament)
    suspend fun getByIdOrNull(id: TournamentId): Tournament?
    suspend fun edit(id: TournamentId, tournament: Tournament)
    fun getAll(): Flow<Tournament>
}
