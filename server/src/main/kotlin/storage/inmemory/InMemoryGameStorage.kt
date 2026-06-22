package me.nekoalice.mafia.api.server.storage.inmemory

import me.nekoalice.mafia.api.dto.game.NewGameBody
import me.nekoalice.mafia.api.dto.tournament.TournamentId
import me.nekoalice.mafia.api.server.storage.base.GameStorage

class InMemoryGameStorage : GameStorage {
    private val games = mutableListOf<NewGameBody>()

    override suspend fun create(game: NewGameBody) {
        games.add(game)
    }

    override suspend fun getAll(tournamentId: TournamentId?): List<NewGameBody> =
        games.filter { tournamentId == null || it.tournament == tournamentId }.toList()
}
