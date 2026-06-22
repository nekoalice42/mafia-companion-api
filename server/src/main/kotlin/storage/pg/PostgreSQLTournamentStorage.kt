package me.nekoalice.mafia.api.server.storage.pg

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import me.nekoalice.mafia.api.dao.Tournaments
import me.nekoalice.mafia.api.dto.tournament.Tournament
import me.nekoalice.mafia.api.dto.tournament.TournamentId
import me.nekoalice.mafia.api.server.storage.base.TournamentStorage
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.upsert
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class PostgreSQLTournamentStorage : TournamentStorage {
    override suspend fun getByIdOrNull(id: TournamentId): Tournament? = readonlyTx {
        Tournaments.selectAll()
            .where { Tournaments.id eq id.value }
            .map(::tournamentFromDao)
            .singleOrNull()
    }

    override suspend fun editOrAdd(
        id: TournamentId,
        item: Tournament
    ) {
        tx {
            Tournaments.upsert(Tournaments.id) {
                it[Tournaments.id] = id.value
                it[Tournaments.name] = item.name
                it[Tournaments.startsAt] = item.startDate
                it[Tournaments.endsAt] = item.endDate
            }
        }
    }

    override suspend fun getAll(): List<Tournament> = readonlyTx {
        Tournaments.selectAll().map(::tournamentFromDao).toList()
    }

    override suspend fun delete(id: TournamentId) {
        tx {
            Tournaments.deleteWhere { Tournaments.id eq id.value }
        }
    }
}
