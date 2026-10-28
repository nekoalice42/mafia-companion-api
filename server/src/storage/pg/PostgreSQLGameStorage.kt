package me.nekoalice.mafia.api.server.storage.pg

import kotlinx.coroutines.flow.groupBy
import kotlinx.coroutines.flow.toList
import me.nekoalice.mafia.api.dao.Games
import me.nekoalice.mafia.api.dao.InGamePlayers
import me.nekoalice.mafia.api.dto.game.Game
import me.nekoalice.mafia.api.dto.game.GameId
import me.nekoalice.mafia.api.dto.game.InputGame
import me.nekoalice.mafia.api.dto.tournament.TournamentId
import me.nekoalice.mafia.api.server.storage.base.GameStorage
import org.jetbrains.exposed.v1.core.andIfNotNull
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.r2dbc.batchInsert
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import kotlin.time.Instant

class PostgreSQLGameStorage : GameStorage {
    override suspend fun create(tournamentId: TournamentId, game: InputGame): GameId {
        val id = GameId.new()
        tx {
            Games.insert {
                it[this.id] = id.value
                it[this.tournamentId] = tournamentId.value
                it[winnerTeam] = game.winnerTeam?.toDao()
                it[startedAt] = game.startTime
            }
            InGamePlayers.batchInsert(game.players.withIndex()) { (index, player) ->
                this[InGamePlayers.gameId] = id.value
                this[InGamePlayers.playerId] = player.playerId.value
                this[InGamePlayers.seat] = index + 1
                this[InGamePlayers.role] = player.role.toDao()
                this[InGamePlayers.extraPoints] = player.extraPoints?.pointsX100
                this[InGamePlayers.extraPointsDescription] = player.extraPoints?.description
                this[InGamePlayers.guessedMafiaCount] = player.guessedMafiaCount
            }
        }
        return id
    }

    override suspend fun getAllByTournamentId(tournamentId: TournamentId, until: Instant?): List<Game> = readonlyTx {
        val games = Games.selectAll()
            .where {
                (Games.tournamentId eq tournamentId.value) andIfNotNull
                        until?.let { Games.startedAt less it }
            }.toList()
        if (games.isEmpty()) return@readonlyTx emptyList()

        val gameIds = games.map { it[Games.id].value }
        val playersByGame = InGamePlayers.selectAll()
            .where { InGamePlayers.gameId inList gameIds }
            .orderBy(InGamePlayers.seat)
            .groupBy(
                { it[InGamePlayers.gameId].value },
                ::inGamePlayerFromDao,
            )

        games.map { gameFromDao(it, playersByGame[it[Games.id].value]!!) }
    }

    override suspend fun delete(id: GameId) {
        tx {
            InGamePlayers.deleteWhere { InGamePlayers.gameId eq id.value }
            Games.deleteWhere { Games.id eq id.value }
        }
    }
}
