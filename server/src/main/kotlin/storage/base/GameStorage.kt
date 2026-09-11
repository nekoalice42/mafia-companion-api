package me.nekoalice.mafia.api.server.storage.base

import me.nekoalice.mafia.api.dto.game.Game
import me.nekoalice.mafia.api.dto.game.GameId
import me.nekoalice.mafia.api.dto.tournament.TournamentId

interface GameStorage {
    suspend fun create(tournamentId: TournamentId, game: Game)
    suspend fun getAllByTournamentId(tournamentId: TournamentId): List<Game>
    suspend fun delete(id: GameId)
}
