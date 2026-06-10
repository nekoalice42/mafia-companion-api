package me.nekoalice.mafia.api.server.storage.base

import kotlinx.coroutines.flow.Flow
import me.nekoalice.mafia.api.dto.models.NewGameBody
import me.nekoalice.mafia.api.dto.models.TournamentId

interface GameStorage {
    suspend fun create(game: NewGameBody)
    suspend fun getAll(tournamentId: TournamentId?): List<NewGameBody>
}
