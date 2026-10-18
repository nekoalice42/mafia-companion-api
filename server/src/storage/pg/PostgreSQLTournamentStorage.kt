package me.nekoalice.mafia.api.server.storage.pg

import kotlinx.coroutines.flow.groupBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import me.nekoalice.mafia.api.dao.TournamentPlayers
import me.nekoalice.mafia.api.dao.Tournaments
import me.nekoalice.mafia.api.dto.tournament.InputTournament
import me.nekoalice.mafia.api.dto.tournament.Tournament
import me.nekoalice.mafia.api.dto.tournament.TournamentId
import me.nekoalice.mafia.api.server.storage.base.TournamentStorage
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.notInList
import org.jetbrains.exposed.v1.r2dbc.batchInsert
import org.jetbrains.exposed.v1.r2dbc.batchUpsert
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update

class PostgreSQLTournamentStorage : TournamentStorage {
    override suspend fun add(item: InputTournament): TournamentId {
        val id = TournamentId.new()
        tx {
            Tournaments.insert {
                it[this.id] = id.value
                it[name] = item.name
                it[startsAt] = item.startDate
                it[endsAt] = item.endDate
            }
            TournamentPlayers.batchInsert(item.participatingPlayers) {
                this[TournamentPlayers.tournamentId] = id.value
                this[TournamentPlayers.playerId] = it.playerId.value
                this[TournamentPlayers.startingPoints] = it.startingPointsX100
            }
        }
        return id
    }

    override suspend fun getByIdOrNull(id: TournamentId): Tournament? = readonlyTx {
        Tournaments.selectAll()
            .where { Tournaments.id eq id.value }
            .singleOrNull()
            ?.let {
                val players = TournamentPlayers.selectAll()
                    .where { TournamentPlayers.tournamentId eq id.value }
                    .map(::playerStartingPointsFromDao)
                    .toList()
                tournamentFromDao(it, players)
            }
    }

    override suspend fun edit(id: TournamentId, item: InputTournament) {
        tx {
            Tournaments.update(where = { Tournaments.id eq id.value }) {
                it[this.id] = id.value
                it[name] = item.name
                it[startsAt] = item.startDate
                it[endsAt] = item.endDate
            }
            TournamentPlayers.deleteWhere {
                val participatingPlayerIds = item.participatingPlayers.map { it.playerId.value }
                val isTournamentPlayer = TournamentPlayers.tournamentId eq id.value
                val nonParticipatingPlayerIds =
                    TournamentPlayers.playerId notInList participatingPlayerIds

                if (participatingPlayerIds.isNotEmpty())
                    isTournamentPlayer and nonParticipatingPlayerIds
                else
                    isTournamentPlayer
            }
            TournamentPlayers.batchUpsert(item.participatingPlayers) {
                this[TournamentPlayers.tournamentId] = id.value
                this[TournamentPlayers.playerId] = it.playerId.value
                this[TournamentPlayers.startingPoints] = it.startingPointsX100
            }
        }
    }

    override suspend fun getAll(): List<Tournament> = readonlyTx {
        val tournaments = Tournaments.selectAll().toList()
        val playersByTournament = TournamentPlayers.selectAll()
            .groupBy(
                { it[TournamentPlayers.tournamentId].value },
                ::playerStartingPointsFromDao,
            )
        tournaments.map {
            tournamentFromDao(it, playersByTournament[it[Tournaments.id].value].orEmpty())
        }
    }

    override suspend fun delete(id: TournamentId) {
        tx {
            TournamentPlayers.deleteWhere { TournamentPlayers.tournamentId eq id.value }
            Tournaments.deleteWhere { Tournaments.id eq id.value }
        }
    }
}
