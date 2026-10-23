package me.nekoalice.mafia.api.server.storage.inmemory

import me.nekoalice.mafia.api.dto.game.Game
import me.nekoalice.mafia.api.dto.game.GameId
import me.nekoalice.mafia.api.dto.game.InputGame
import me.nekoalice.mafia.api.dto.tournament.TournamentId
import me.nekoalice.mafia.api.server.storage.base.GameStorage
import kotlin.time.Instant

class InMemoryGameStorage : GameStorage {
    private val games = mutableMapOf<TournamentId, MutableList<Game>>()

    override suspend fun create(tournamentId: TournamentId, game: InputGame): GameId =
        GameId.new().also { id ->
            // NOTE: this implementation doesn't ensure that game id is unique across tournaments,
            //  while pg storage implementation does.
            games.getOrPut(tournamentId) { mutableListOf() }.add(game.toModel(id))
        }

    override suspend fun getAllByTournamentId(tournamentId: TournamentId, until: Instant?): List<Game> =
        games[tournamentId]
            ?.filter { until == null || it.startTime < until }
            ?: emptyList()

    override suspend fun delete(id: GameId) {
        for (tournamentGames in games.values) {
            tournamentGames.removeAll { it.id == id }
        }
    }
}
