package me.nekoalice.mafia.api.server

import io.ktor.http.*
import io.ktor.server.auth.AuthenticationFailedCause
import me.nekoalice.mafia.api.contracts.APIInfo
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.dto.auth.*
import me.nekoalice.mafia.api.dto.game.NewGameBody
import me.nekoalice.mafia.api.dto.health.HealthResponse
import me.nekoalice.mafia.api.dto.health.HelloResponse
import me.nekoalice.mafia.api.dto.player.Player
import me.nekoalice.mafia.api.dto.player.PlayerId
import me.nekoalice.mafia.api.dto.response.ResponseList
import me.nekoalice.mafia.api.dto.tournament.Tournament
import me.nekoalice.mafia.api.dto.tournament.TournamentId
import me.nekoalice.mafia.api.dto.tournament.scoreboard.ScoreboardRow
import me.nekoalice.mafia.api.dto.user.User
import me.nekoalice.mafia.api.dto.user.UserId
import me.nekoalice.mafia.api.server.storage.base.CRUDStorage
import me.nekoalice.mafia.api.server.storage.base.StorageProvider
import me.nekoalice.mafia.api.server.storage.base.UserStorage
import me.nekoalice.mafia.api.server.utils.calculateScoreboard
import me.nekoalice.mafia.api.server.utils.parseAndVerifyTelegramToken
import me.nekoalice.mafia.api.server.validation.validate
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

private val accessTokenExpiration = 30.minutes
private val refreshTokenExpiration = 7.days

class APIImpl(
    private val storages: StorageProvider,
    private val telegramOidcClientId: String,
) : BaseAPI(
    info = APIInfo(
        name = "mafia-companion-api",
        version = "0.1.0-alpha.5",
        licenseIdentifier = "AGPL-3.0",
        developmentUrl = "http://localhost:8080",
        productionUrl = "https://api.mafia.nekoalice.me",
    ),
) {
    private suspend fun <StorageT : CRUDStorage<ItemT, IdT>, ItemT, IdT> upsert(
        storage: StorageT,
        item: ItemT,
        id: IdT,
    ): Response<Unit> {
        val existed = storage.getByIdOrNull(id) != null
        storage.editOrAdd(id, item)
        return when (existed) {
            true -> Response.Success(HttpStatusCode.NoContent)
            false -> Response.Success(HttpStatusCode.Created)
        }
    }

    private suspend fun recreateTokenPair(userId: UserId): TokenPair {
        val tokens = storages.auth.recreateTokenPair(
            userId = userId,
            currentTime = Clock.System.now(),
            accessTokenExpiration = accessTokenExpiration,
            refreshTokenExpiration = refreshTokenExpiration,
        )
        return TokenPair(
            access = TokenInfo(
                value = tokens.access,
                expiresInSeconds = accessTokenExpiration.inWholeSeconds.toULong(),
            ),
            refresh = TokenInfo(
                value = tokens.refresh,
                expiresInSeconds = refreshTokenExpiration.inWholeSeconds.toULong(),
            ),
        )
    }

    override suspend fun getRoot(): Response<HelloResponse> =
        Response.Success(HelloResponse())

    override suspend fun getHealth(): Response<HealthResponse> {
        val isDatabaseHealthy = try {
            storages.ping()
            true
        } catch (_: Exception) {
            false
        }

        val health = HealthResponse(
            isDatabaseHealthy = isDatabaseHealthy,
        )

        return Response.Success(
            health,
            if (health.status) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable,
        )
    }

    override suspend fun login(loginData: LoginData): Response<TokenPair> {
        if (!loginData.validate()) {
            return Response.Error("Invalid login data", HttpStatusCode.UnprocessableEntity)
        }
        val user = storages.user.getByUsernameOrNull(loginData.username) ?: return Response.Error(
            "Invalid username",
            HttpStatusCode.Unauthorized,
        )
        if (!storages.auth.verifyPassword(user.id, loginData.password)) {
            return Response.Error("Invalid password", HttpStatusCode.Unauthorized)
        }
        return Response.Success(recreateTokenPair(user.id))
    }

    override suspend fun getMe(userId: UserId): Response<User> =
        Response.Success(storages.user.getByIdOrNull(userId)!!)

    override suspend fun changePassword(loginData: LoginData): Response<Unit> {
        if (!loginData.validate()) {
            return Response.Error("Invalid login data", HttpStatusCode.UnprocessableEntity)
        }
        val user = storages.user.getByUsernameOrNull(loginData.username) ?: return Response.Error(
            "User not found",
            HttpStatusCode.BadRequest,
        )
        storages.auth.setPassword(user.id, loginData.password)
        return Response.Success(HttpStatusCode.NoContent)
    }

    override suspend fun refreshLogin(refreshToken: RefreshToken): Response<TokenPair> {
        val userId = storages.auth.verifyRefreshTokenOrNull(refreshToken.value)
            ?: return Response.Error(
                "Invalid refresh token",
                HttpStatusCode.Unauthorized,
            )
        return Response.Success(recreateTokenPair(userId))
    }

    override suspend fun logoutAll(userId: UserId): Response<Unit> {
        storages.auth.revokeTokens(userId)
        return Response.Success(HttpStatusCode.NoContent)
    }

    override suspend fun getPlayers(): Response<ResponseList<Player>> =
        Response.Success(ResponseList(storages.player.getAll()))

    override suspend fun getPlayer(playerId: PlayerId): Response<Player> =
        storages.player.getByIdOrNull(playerId)?.let {
            Response.Success(it)
        } ?: Response.Error("Player not found", HttpStatusCode.NotFound)

    override suspend fun upsertPlayer(player: Player): Response<Unit> =
        upsert(storages.player, player, player.id)

    override suspend fun deletePlayer(playerId: PlayerId): Response<Unit> {
        storages.player.delete(playerId)
        return Response.Success(HttpStatusCode.NoContent)
    }

    override suspend fun getTournaments(): Response<ResponseList<Tournament>> {
        return Response.Success(ResponseList(storages.tournament.getAll()))
    }

    override suspend fun getTournament(id: TournamentId): Response<Tournament> =
        storages.tournament.getByIdOrNull(id)?.let {
            Response.Success(it)
        } ?: Response.Error("Tournament not found", HttpStatusCode.NotFound)

    override suspend fun upsertTournament(tournament: Tournament): Response<Unit> =
        upsert(storages.tournament, tournament, tournament.id)

    override suspend fun deleteTournament(tournamentId: TournamentId): Response<Unit> {
        storages.tournament.delete(tournamentId)
        return Response.Success(HttpStatusCode.NoContent)
    }

    override suspend fun createGame(game: NewGameBody): Response<Unit> {
        if (storages.tournament.getByIdOrNull(game.tournament) == null) {
            return Response.Error(
                "Tournament ${game.tournament} not found",
                HttpStatusCode.NotFound,
            )
        }
        try {
            game.validate()
        } catch (e: IllegalArgumentException) {
            return Response.Error(e, HttpStatusCode.UnprocessableEntity)
        }
        storages.game.create(game)
        return Response.Success(HttpStatusCode.Created)
    }

    override suspend fun getScoreboard(
        tournamentId: TournamentId,
    ): Response<ResponseList<ScoreboardRow>> {
        storages.tournament.getByIdOrNull(tournamentId) ?: return Response.Error(
            "Tournament $tournamentId not found",
            HttpStatusCode.NotFound,
        )
        val scoreboardSorted = calculateScoreboard(storages.game.getAll(tournamentId))
            .map {
                it.toScoreboardRow(
                    storages.player.getByIdOrNull(it.playerId)
                        ?: Player(it.playerId, "Unknown player ${it.playerId}"),
                )
            }
            .sortedWith(
                compareByDescending<ScoreboardRow> { it.totalPointsX100 }
                    .thenByDescending { it.playCount.total }
                    .thenByDescending { it.winCount.total }
                    .thenBy { it.player.nickname },
            )
        return Response.Success(ResponseList(scoreboardSorted))
    }

    override suspend fun telegramOauthCallback(token: TelegramIdToken): Response<TokenPair> {
        val identity = parseAndVerifyTelegramToken(token.value, telegramOidcClientId).let {
            if (it.isFailure) {
                return Response.Error(
                    "Telegram login flow failed",
                    HttpStatusCode.ServiceUnavailable,
                )
            }
            it.getOrThrow()
        }
        val user = storages.user.getByExternalIdOrNull(
            identity.id.toString(),
            UserStorage.ExternalUserProvider.Telegram,
        ) ?: return Response.Error(
            "No such Telegram user registered (id=${identity.id})",
            HttpStatusCode.Forbidden,
        )
        return Response.Success(recreateTokenPair(user.id))
    }

    override suspend fun handleAuthentication(token: AccessToken): UserId? =
        storages.auth.verifyAccessTokenOrNull(token.value)

    override suspend fun handleTelegramOauthError(
        cause: AuthenticationFailedCause.Error,
    ): Response<Nothing> = Response.Error(
        "Telegram login flow failed: ${cause.message}",
        HttpStatusCode.ServiceUnavailable,
    )
}
