package me.nekoalice.mafia.api.server.storage.pg

import me.nekoalice.mafia.api.dao.*
import me.nekoalice.mafia.api.dto.game.ExtraPointsDescribed
import me.nekoalice.mafia.api.dto.game.Game
import me.nekoalice.mafia.api.dto.game.GameId
import me.nekoalice.mafia.api.dto.game.InGamePlayer
import me.nekoalice.mafia.api.dto.game.enums.Team
import me.nekoalice.mafia.api.dto.player.Player
import me.nekoalice.mafia.api.dto.player.PlayerId
import me.nekoalice.mafia.api.dto.tournament.PlayerStartingPoints
import me.nekoalice.mafia.api.dto.tournament.Tournament
import me.nekoalice.mafia.api.dto.tournament.TournamentId
import me.nekoalice.mafia.api.dto.user.User
import me.nekoalice.mafia.api.dto.user.UserId
import me.nekoalice.mafia.api.server.storage.base.UserStorage
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import me.nekoalice.mafia.api.dao.Role as DaoRole
import me.nekoalice.mafia.api.dto.game.enums.Role as DtoRole

@IgnorableReturnValue
internal suspend fun <T> tx(block: suspend R2dbcTransaction.() -> T) = suspendTransaction(
    readOnly = false,
    statement = block,
)

@IgnorableReturnValue
internal suspend fun <T> readonlyTx(block: suspend R2dbcTransaction.() -> T) = suspendTransaction(
    readOnly = true,
    statement = block,
)

internal fun mapDtoTeam(team: Team): WinnerTeam = when (team) {
    Mafia -> MAFIA
    Citizen -> CITIZEN
}

internal fun mapDtoRole(role: DtoRole): DaoRole = when (role) {
    Mafia -> MAFIA
    Don -> DON
    Sheriff -> SHERIFF
    Citizen -> CITIZEN
}

internal fun mapProvider(provider: UserStorage.ExternalUserProvider): ExternalProvider =
    when (provider) {
        Telegram -> TELEGRAM
    }

internal fun mapDaoWinnerTeam(team: WinnerTeam): Team = when (team) {
    MAFIA -> Mafia
    CITIZEN -> Citizen
}

internal fun mapDaoRole(role: DaoRole): DtoRole = when (role) {
    MAFIA -> Mafia
    DON -> Don
    SHERIFF -> Sheriff
    CITIZEN -> Citizen
}

internal fun gameFromDao(row: ResultRow, players: List<InGamePlayer>) = Game(
    id = GameId(row[Games.id].value),
    players = players,
    winnerTeam = row[Games.winnerTeam]?.let(::mapDaoWinnerTeam),
    startTime = row[Games.startedAt],
)

internal fun inGamePlayerFromDao(row: ResultRow) = InGamePlayer(
    playerId = PlayerId(row[InGamePlayers.playerId].value),
    role = mapDaoRole(row[InGamePlayers.role]),
    extraPoints = if (row[InGamePlayers.extraPoints] != null)
        ExtraPointsDescribed(
            pointsX100 = row[InGamePlayers.extraPoints]!!,
            description = row[InGamePlayers.extraPointsDescription]!!,
        ) else null,
    guessedMafiaCount = row[InGamePlayers.guessedMafiaCount],
)

internal fun playerFromDao(row: ResultRow): Player =
    Player(
        id = PlayerId(row[Players.id].value),
        nickname = row[Players.nickname],
    )

internal fun playerStartingPointsFromDao(row: ResultRow) = PlayerStartingPoints(
    playerId = PlayerId(row[TournamentPlayers.playerId].value),
    startingPointsX100 = row[TournamentPlayers.startingPoints],
)

internal fun tournamentFromDao(
    row: ResultRow,
    participatingPlayers: List<PlayerStartingPoints>,
) = Tournament(
    id = TournamentId(row[Tournaments.id].value),
    name = row[Tournaments.name],
    startsAt = row[Tournaments.startsAt],
    endsAt = row[Tournaments.endsAt],
    participatingPlayers = participatingPlayers,
    scoreboardFreezesAt = row[Tournaments.scoreboardFreezesAt],
)

internal fun userFromDao(row: ResultRow): User =
    User(
        id = UserId(row[Users.id].value),
        username = row[Users.username],
    )
