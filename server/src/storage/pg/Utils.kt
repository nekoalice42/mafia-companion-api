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
import me.nekoalice.mafia.api.dto.user.external.ExternalUserBind
import me.nekoalice.mafia.api.dto.user.external.ExternalUserProvider
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import me.nekoalice.mafia.api.dao.Role as DaoRole
import me.nekoalice.mafia.api.dao.UserRole as DaoUserRole
import me.nekoalice.mafia.api.dto.game.enums.Role as DtoRole
import me.nekoalice.mafia.api.dto.user.UserRole as DtoUserRole

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

internal fun Team.toDao(): WinnerTeam = when (this) {
    Mafia -> MAFIA
    Citizen -> CITIZEN
}

internal fun DtoRole.toDao(): DaoRole = when (this) {
    Mafia -> MAFIA
    Don -> DON
    Sheriff -> SHERIFF
    Citizen -> CITIZEN
}

internal fun DtoUserRole.toDao(): DaoUserRole = when (this) {
    Admin -> ADMIN
    Judge -> JUDGE
}

internal fun ExternalUserProvider.toDao(): ExternalProvider = when (this) {
    Telegram -> TELEGRAM
}

internal fun WinnerTeam.toDto(): Team = when (this) {
    MAFIA -> Mafia
    CITIZEN -> Citizen
}

internal fun DaoRole.toDto(): DtoRole = when (this) {
    MAFIA -> Mafia
    DON -> Don
    SHERIFF -> Sheriff
    CITIZEN -> Citizen
}

internal fun DaoUserRole.toDto(): DtoUserRole = when (this) {
    ADMIN -> Admin
    JUDGE -> Judge
}

internal fun ExternalProvider.toDto(): ExternalUserProvider = when (this) {
    TELEGRAM -> Telegram
}

internal fun gameFromDao(row: ResultRow, players: List<InGamePlayer>) = Game(
    id = GameId(row[Games.id].value),
    players = players,
    winnerTeam = row[Games.winnerTeam]?.toDto(),
    startTime = row[Games.startedAt],
)

internal fun inGamePlayerFromDao(row: ResultRow) = InGamePlayer(
    playerId = PlayerId(row[InGamePlayers.playerId].value),
    role = row[InGamePlayers.role].toDto(),
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
        role = row[Users.role].toDto(),
    )

internal fun externalUserBindFromDao(row: ResultRow): ExternalUserBind =
    ExternalUserBind(
        provider = row[ExternalUsers.provider].value.toDto(),
        externalId = row[ExternalUsers.externalId].value,
    )
