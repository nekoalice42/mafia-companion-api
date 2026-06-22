package me.nekoalice.mafia.api.server.storage.base

import me.nekoalice.mafia.api.dto.game.NewGameBody
import me.nekoalice.mafia.api.dto.tournament.TournamentId

interface GameStorage {
    suspend fun create(game: NewGameBody)
    suspend fun getAll(tournamentId: TournamentId?): List<NewGameBody>
}
