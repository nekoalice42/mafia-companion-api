package me.nekoalice.mafia.api.server.storage.pg

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import me.nekoalice.mafia.api.dao.Games
import me.nekoalice.mafia.api.dao.InGamePlayers
import me.nekoalice.mafia.api.dto.models.*
import me.nekoalice.mafia.api.server.storage.base.GameStorage
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.batchInsert
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.selectAll
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class PostgreSQLGameStorage : GameStorage {
    override suspend fun create(game: NewGameBody) {
        tx {
            val newGameId = Games.insertAndGetId {
                it[tournamentId] = game.tournament.value
                it[winnerTeam] = mapDtoTeam(game.winnerTeam)
                it[startedAt] = game.startTime
            }
            InGamePlayers.batchInsert(game.players) {
                this[InGamePlayers.gameId] = newGameId
                this[InGamePlayers.playerId] = it.playerId.value
                this[InGamePlayers.role] = mapDtoRole(it.role)
                this[InGamePlayers.extraPoints] = it.extraPoints?.pointsX100
                this[InGamePlayers.extraPointsDescription] = it.extraPoints?.description
                this[InGamePlayers.guessedMafiaCount] = it.guessedMafiaCount
            }
        }
    }

    override suspend fun getAll(tournamentId: TournamentId?): List<NewGameBody> = readonlyTx {
        var query = Games.selectAll()
        if (tournamentId != null) {
            query = query.where { Games.tournamentId eq tournamentId.value }
        }
        query.map {
            val players = InGamePlayers.selectAll()
                .where { InGamePlayers.gameId eq it[Games.id] }
                .map { igp ->
                    InGamePlayer(
                        playerId = PlayerId(igp[InGamePlayers.playerId].value),
                        role = mapDaoRole(igp[InGamePlayers.role]),
                        extraPoints = if (igp[InGamePlayers.extraPoints] != null)
                            ExtraPointsDescribed(
                                pointsX100 = igp[InGamePlayers.extraPoints]!!,
                                description = igp[InGamePlayers.extraPointsDescription]!!,
                            ) else null,
                        guessedMafiaCount = igp[InGamePlayers.guessedMafiaCount],
                    )
                }.toList()
            NewGameBody(
                tournament = TournamentId(it[Games.tournamentId]),
                players = players,
                winnerTeam = mapDaoWinnerTeam(it[Games.winnerTeam]),
                startTime = it[Games.startedAt],
            )
        }.toList()
    }
}
