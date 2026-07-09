package me.nekoalice.mafia.api.server.storage.pg

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import me.nekoalice.mafia.api.dao.Games
import me.nekoalice.mafia.api.dao.InGamePlayers
import me.nekoalice.mafia.api.dto.game.ExtraPointsDescribed
import me.nekoalice.mafia.api.dto.game.InGamePlayer
import me.nekoalice.mafia.api.dto.game.NewGameBody
import me.nekoalice.mafia.api.dto.player.PlayerId
import me.nekoalice.mafia.api.dto.tournament.TournamentId
import me.nekoalice.mafia.api.server.storage.base.GameStorage
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.batchInsert
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.selectAll

class PostgreSQLGameStorage : GameStorage {
    override suspend fun create(game: NewGameBody) {
        tx {
            val newGameId = Games.insertAndGetId {
                it[tournamentId] = game.tournament.value
                it[winnerTeam] = game.winnerTeam?.let(::mapDtoTeam)
                it[startedAt] = game.startTime
            }
            InGamePlayers.batchInsert(game.players.withIndex()) { (index, player) ->
                this[InGamePlayers.gameId] = newGameId
                this[InGamePlayers.playerId] = player.playerId.value
                this[InGamePlayers.seat] = index + 1
                this[InGamePlayers.role] = mapDtoRole(player.role)
                this[InGamePlayers.extraPoints] = player.extraPoints?.pointsX100
                this[InGamePlayers.extraPointsDescription] = player.extraPoints?.description
                this[InGamePlayers.guessedMafiaCount] = player.guessedMafiaCount
            }
        }
    }

    override suspend fun getAll(tournamentId: TournamentId?): List<NewGameBody> = readonlyTx {
        var query = Games.selectAll()
        if (tournamentId != null) {
            query = query.where { Games.tournamentId eq tournamentId.value }
        }
        val gamesRaw = query.toList()
        gamesRaw.map { gamesResult ->
            val players = InGamePlayers.selectAll()
                .where { InGamePlayers.gameId eq gamesResult[Games.id] }
                .orderBy(InGamePlayers.seat)
                .map { playersResult ->
                    InGamePlayer(
                        playerId = PlayerId(playersResult[InGamePlayers.playerId].value),
                        role = mapDaoRole(playersResult[InGamePlayers.role]),
                        extraPoints = if (playersResult[InGamePlayers.extraPoints] != null)
                            ExtraPointsDescribed(
                                pointsX100 = playersResult[InGamePlayers.extraPoints]!!,
                                description = playersResult[InGamePlayers.extraPointsDescription]!!,
                            ) else null,
                        guessedMafiaCount = playersResult[InGamePlayers.guessedMafiaCount],
                    )
                }.toList()
            NewGameBody(
                tournament = TournamentId(gamesResult[Games.tournamentId].value),
                players = players,
                winnerTeam = gamesResult[Games.winnerTeam]?.let(::mapDaoWinnerTeam),
                startTime = gamesResult[Games.startedAt],
            )
        }.toList()
    }
}
