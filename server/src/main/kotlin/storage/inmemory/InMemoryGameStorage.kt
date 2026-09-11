package me.nekoalice.mafia.api.server.storage.inmemory

import me.nekoalice.mafia.api.dto.game.Game
import me.nekoalice.mafia.api.dto.game.GameId
import me.nekoalice.mafia.api.dto.tournament.TournamentId
import me.nekoalice.mafia.api.server.storage.base.GameStorage

class InMemoryGameStorage : GameStorage {
    private val games = mutableMapOf<TournamentId, MutableList<Game>>()

    override suspend fun create(tournamentId: TournamentId, game: Game) {
        // NOTE: this implementation doesn't ensure that game id is unique across tournaments,
        //  while pg storage implementation does.
        games.getOrPut(tournamentId) { mutableListOf() }.add(game)
    }

    override suspend fun getAllByTournamentId(tournamentId: TournamentId): List<Game> =
        games[tournamentId]?.toList() ?: emptyList()

    override suspend fun delete(id: GameId) {
        for (tournamentGames in games.values) {
            tournamentGames.removeAll { it.id == id }
        }
    }
}
