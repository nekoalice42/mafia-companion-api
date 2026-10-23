package me.nekoalice.mafia.api.server.storage.base

import me.nekoalice.mafia.api.dto.game.Game
import me.nekoalice.mafia.api.dto.game.GameId
import me.nekoalice.mafia.api.dto.game.InputGame
import me.nekoalice.mafia.api.dto.tournament.TournamentId
import kotlin.time.Instant

interface GameStorage {
    suspend fun create(tournamentId: TournamentId, game: InputGame): GameId
    suspend fun getAllByTournamentId(
        tournamentId: TournamentId,
        until: Instant? = null,
    ): List<Game>
    suspend fun delete(id: GameId)
}
