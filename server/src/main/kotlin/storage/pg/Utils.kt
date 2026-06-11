package me.nekoalice.mafia.api.server.storage.pg

import me.nekoalice.mafia.api.dao.Players
import me.nekoalice.mafia.api.dao.Tournaments
import me.nekoalice.mafia.api.dao.WinnerTeam
import me.nekoalice.mafia.api.dto.models.*
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.uuid.ExperimentalUuidApi
import me.nekoalice.mafia.api.dao.Role as DaoRole
import me.nekoalice.mafia.api.dto.models.Role as DtoRole

internal suspend fun <T> tx(block: suspend R2dbcTransaction.() -> T) = suspendTransaction(
    readOnly = false,
    statement = block,
)

internal suspend fun <T> readonlyTx(block: suspend R2dbcTransaction.() -> T) = suspendTransaction(
    readOnly = true,
    statement = block,
)

internal fun mapDtoTeam(team: Team): WinnerTeam = when (team) {
    Team.Mafia -> WinnerTeam.MAFIA
    Team.Citizen -> WinnerTeam.CITIZEN
}

internal fun mapDtoRole(role: DtoRole): DaoRole = when (role) {
    DtoRole.Mafia -> DaoRole.MAFIA
    DtoRole.Don -> DaoRole.DON
    DtoRole.Sheriff -> DaoRole.SHERIFF
    DtoRole.Citizen -> DaoRole.CITIZEN
}

internal fun mapDaoWinnerTeam(team: WinnerTeam): Team = when (team) {
    WinnerTeam.MAFIA -> Team.Mafia
    WinnerTeam.CITIZEN -> Team.Citizen
}

internal fun mapDaoRole(role: DaoRole): DtoRole = when (role) {
    DaoRole.MAFIA -> DtoRole.Mafia
    DaoRole.DON -> DtoRole.Don
    DaoRole.SHERIFF -> DtoRole.Sheriff
    DaoRole.CITIZEN -> DtoRole.Citizen
}

@OptIn(ExperimentalUuidApi::class)
internal fun playerFromDao(row: ResultRow): Player =
    Player(
        id = PlayerId(row[Players.id].value),
        nickname = row[Players.nickname],
    )

@OptIn(ExperimentalUuidApi::class)
internal fun tournamentFromDao(row: ResultRow): Tournament =
    Tournament(
        id = TournamentId(row[Tournaments.id].value),
        name = row[Tournaments.name],
        startDate = row[Tournaments.startsAt],
        endDate = row[Tournaments.endsAt],
    )
