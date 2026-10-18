package me.nekoalice.mafia.api.server

import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.server.auth.AuthenticationFailedCause
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.CannotTransformContentToTypeException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import me.nekoalice.mafia.api.contracts.APIInfo
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.dto.auth.*
import me.nekoalice.mafia.api.dto.base.CRUDModel
import me.nekoalice.mafia.api.dto.base.InputCRUDModel
import me.nekoalice.mafia.api.dto.game.Game
import me.nekoalice.mafia.api.dto.game.GameId
import me.nekoalice.mafia.api.dto.game.InputGame
import me.nekoalice.mafia.api.dto.health.HealthResponse
import me.nekoalice.mafia.api.dto.health.HelloResponse
import me.nekoalice.mafia.api.dto.player.InputPlayer
import me.nekoalice.mafia.api.dto.player.Player
import me.nekoalice.mafia.api.dto.player.PlayerId
import me.nekoalice.mafia.api.dto.response.NewResourceId
import me.nekoalice.mafia.api.dto.response.ResponseList
import me.nekoalice.mafia.api.dto.tournament.InputTournament
import me.nekoalice.mafia.api.dto.tournament.Tournament
import me.nekoalice.mafia.api.dto.tournament.TournamentId
import me.nekoalice.mafia.api.dto.tournament.scoreboard.ScoreboardRow
import me.nekoalice.mafia.api.dto.user.User
import me.nekoalice.mafia.api.dto.user.UserId
import me.nekoalice.mafia.api.server.storage.base.AuthStorage
import me.nekoalice.mafia.api.server.storage.base.CRUDStorage
import me.nekoalice.mafia.api.server.storage.base.StorageProvider
import me.nekoalice.mafia.api.server.storage.base.UserStorage
import me.nekoalice.mafia.api.server.utils.*
import me.nekoalice.mafia.api.server.validation.validate
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

private val accessTokenExpiration = 30.minutes
private val refreshTokenExpiration = 7.days
private val authCodeExpiration = 10.minutes
private val clientStateExpiration = 15.minutes
private val allowedAppRedirectUrlRegex = Regex("""^mafia-api://auth/[^?&=]+$""")

class APIImpl(
    private val storages: StorageProvider,
    private val telegramOidcClientId: String?,
    urls: EnvironmentUrls,
) : BaseAPI(
    info = APIInfo(
        name = "mafia-companion-api",
        version = "0.1.0-beta.3",
        licenseIdentifier = "AGPL-3.0",
        developmentUrl = urls.dev,
        stagingUrl = urls.stage,
        productionUrl = urls.prod,
    ),
) {
    private suspend fun <
            StorageT : CRUDStorage<ItemT, InputItemT, IdT>,
            ItemT : CRUDModel<IdT>,
            InputItemT : InputCRUDModel<ItemT, IdT>,
            IdT,
            > update(
        storage: StorageT,
        item: InputItemT,
        id: IdT,
        itemName: String,
        beforeEdit: suspend (ItemT) -> Response<Unit>? = { null },
    ): Response<Unit> {
        val existingItem = storage.getByIdOrNull(id) ?: return Response.Error(
            "$itemName with id '$id' not found",
            NotFound,
        )
        val editInterruptionResult = beforeEdit(existingItem)
        if (editInterruptionResult != null) {
            return editInterruptionResult
        }
        storage.edit(id, item)
        return Response.Success()
    }

    private suspend fun createTokenPair(userId: UserId): TokenPair {
        val tokens = storages.auth.createTokenPair(
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

    private fun isRedirectUrlValid(redirectUrl: String): Boolean =
        allowedAppRedirectUrlRegex.matches(redirectUrl)

    override suspend fun getRoot(): Response<HelloResponse> =
        Response.Success(HelloResponse())

    override suspend fun getHealth(): Response<HealthResponse> {
        val isDatabaseHealthy = try {
            storages.ping()
            true
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            false
        }

        val health = HealthResponse(
            apiVersion = info.version,
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
        return Response.Success(createTokenPair(user.id))
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
        return Response.Success()
    }

    override suspend fun refreshLogin(refreshToken: RefreshToken): Response<TokenPair> {
        val userId = storages.auth.verifyRefreshTokenOrNull(refreshToken.value, Clock.System.now())
            ?: return Response.Error(
                "Invalid refresh token",
                HttpStatusCode.Unauthorized,
            )
        storages.auth.revokeRefreshToken(refreshToken.value)
        return Response.Success(createTokenPair(userId))
    }

    override suspend fun logoutAll(userId: UserId): Response<Unit> {
        storages.auth.revokeTokens(userId)
        return Response.Success()
    }

    override suspend fun getPlayers(): Response<ResponseList<Player>> =
        Response.Success(ResponseList(storages.player.getAll()))

    override suspend fun getPlayer(playerId: PlayerId): Response<Player> =
        storages.player.getByIdOrNull(playerId)?.let {
            Response.Success(it)
        } ?: Response.Error("Player not found", NotFound)

    override suspend fun createPlayer(player: InputPlayer): Response<NewResourceId> {
        val id = storages.player.add(player)
        return Response.Success(NewResourceId(id), Created)
    }

    override suspend fun updatePlayer(playerId: PlayerId, player: InputPlayer): Response<Unit> =
        update(storages.player, player, playerId, "Player")

    override suspend fun deletePlayer(playerId: PlayerId): Response<Unit> {
        storages.player.delete(playerId)
        return Response.Success()
    }

    override suspend fun getTournaments(): Response<ResponseList<Tournament>> {
        return Response.Success(ResponseList(storages.tournament.getAll()))
    }

    override suspend fun getTournament(id: TournamentId): Response<Tournament> =
        storages.tournament.getByIdOrNull(id)?.let {
            Response.Success(it)
        } ?: Response.Error("Tournament not found", NotFound)

    override suspend fun getTournamentGames(
        tournamentId: TournamentId,
    ): Response<ResponseList<Game>> {
        if (storages.tournament.getByIdOrNull(tournamentId) == null) {
            return Response.Error(
                "Tournament $tournamentId not found",
                NotFound,
            )
        }
        return Response.Success(ResponseList(storages.game.getAllByTournamentId(tournamentId)))
    }

    override suspend fun createTournament(tournament: InputTournament): Response<NewResourceId> {
        val id = storages.tournament.add(tournament)
        return Response.Success(NewResourceId(id), Created)
    }

    override suspend fun updateTournament(
        tournamentId: TournamentId,
        tournament: InputTournament,
    ): Response<Unit> =
        update(storages.tournament, tournament, tournamentId, "Tournament") { existing ->
            val participatingPlayerIds = existing.participatingPlayerIds
            val newParticipatingPlayerIds = tournament.participatingPlayerIds
            if (
                storages.game.getAllByTournamentId(tournamentId).isNotEmpty()
                && participatingPlayerIds != newParticipatingPlayerIds
            )
                Response.Error(
                    "Cannot change participating players when the tournament already has games",
                    BadRequest,
                )
            else null
        }

    override suspend fun deleteTournament(tournamentId: TournamentId): Response<Unit> {
        storages.tournament.delete(tournamentId)
        return Response.Success()
    }

    override suspend fun createGame(
        tournamentId: TournamentId,
        game: InputGame,
    ): Response<NewResourceId> {
        val tournament = storages.tournament.getByIdOrNull(tournamentId) ?: return Response.Error(
            "Tournament $tournamentId not found",
            NotFound,
        )
        try {
            game.validate()
        } catch (e: IllegalArgumentException) {
            return Response.Error(e, UnprocessableEntity)
        }
        val participatingPlayerIds = tournament.participatingPlayerIds
        if (participatingPlayerIds.isNotEmpty()) {
            for (player in game.players.map { it.playerId }) {
                if (player !in participatingPlayerIds) {
                    return Response.Error(
                        "Player id='$player' is not participating in the tournament",
                        BadRequest,
                    )
                }
            }
        }
        val id = storages.game.create(tournamentId, game)
        return Response.Success(NewResourceId(id), Created)
    }

    override suspend fun deleteGame(gameId: GameId): Response<Unit> {
        storages.game.delete(gameId)
        return Response.Success()
    }

    override suspend fun getScoreboard(
        tournamentId: TournamentId,
    ): Response<ResponseList<ScoreboardRow>> {
        val tournament = storages.tournament.getByIdOrNull(tournamentId) ?: return Response.Error(
            "Tournament $tournamentId not found",
            NotFound,
        )
        val scoreboardSorted = calculateScoreboard(
            storages.game.getAllByTournamentId(tournamentId),
            tournament.participatingPlayers,
        ).map {
            it.toScoreboardRow(
                storages.player.getByIdOrNull(it.playerId)
                    ?: Player(it.playerId, "Unknown player ${it.playerId}"),
            )
        }.sortedWith(
            compareByDescending<ScoreboardRow> { it.totalPointsX100 }
                .thenByDescending { it.playCount.total }
                .thenByDescending { it.extraPointsX100 }
                .thenByDescending { it.winCount.total }
                .thenByDescending { it.winCount.don + it.winCount.sheriff }
                .thenByDescending { it.firstNightDeaths }
                .thenBy { it.player.nickname },
        )
        val scoreboardSortedWithPlaces = setPlaces(scoreboardSorted)
        return Response.Success(ResponseList(scoreboardSortedWithPlaces))
    }

    override suspend fun telegramOauthCallbackHtml(
        token: TelegramIdToken,
        oauthState: String,
    ): Response<Unit> {
        requireNotNull(telegramOidcClientId) { "Telegram OIDC is not configured" }
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
        val code = generateToken(4)
        storages.auth.setUserForAuthCode(
            code = code,
            userId = user.id,
            currentTime = Clock.System.now(),
            expiration = authCodeExpiration,
        )
        val clientState = storages.auth.popClientStateOrNull(oauthState, Clock.System.now())
        if (clientState?.redirectUrl != null) {
            if (!isRedirectUrlValid(clientState.redirectUrl)) {
                return Response.Error(
                    "Bad redirect URL",
                    HttpStatusCode.BadRequest,
                )
            }
            val url = with(URLBuilder(clientState.redirectUrl)) {
                parameters["code"] = code
                clientState.state?.let { parameters["state"] = it }
                buildString()
            }
            return Response.Redirect(url)
        }
        // Copied from [io.ktor.server.html.respondHtml].
        val text = buildString {
            append("<!DOCTYPE html>\n")
            appendHTML().html {
                getTelegramLoginSuccessHtml(code)
            }
        }
        return Response.Raw.html(text)
    }

    override suspend fun finishTelegramChallenge(code: ExternalAuthCode): Response<TokenPair> {
        val userId = storages.auth.popUserForAuthCodeOrNull(code.code, Clock.System.now())
            ?: return Response.Error(
                "No user found for auth code ${code.code}",
                HttpStatusCode.BadRequest,
            )
        return Response.Success(createTokenPair(userId))
    }

    override suspend fun handleAuthentication(token: AccessToken): UserId? =
        storages.auth.verifyAccessTokenOrNull(token.value, Clock.System.now())

    override suspend fun handleNewTelegramOauthState(
        oauthState: String,
        redirectUrl: String?,
        clientState: String?,
    ) {
        storages.auth.setClientState(
            state = oauthState,
            clientState = AuthStorage.ClientState(redirectUrl, clientState),
            currentTime = Clock.System.now(),
            expiration = clientStateExpiration,
        )
    }

    override suspend fun handleTelegramOauthError(
        cause: AuthenticationFailedCause.Error,
    ): Response<Nothing> = Response.Error(
        "Telegram login flow failed: ${cause.message}",
        HttpStatusCode.ServiceUnavailable,
    )

    override suspend fun onBadRequest(cause: BadRequestException): Response<Nothing> =
        Response.Error(cause, BadRequest)

    override suspend fun onBadContentType(cause: CannotTransformContentToTypeException): Response<Nothing> =
        Response.Error(cause, UnsupportedMediaType)

    override suspend fun onUnhandledException(cause: Throwable): Response<Nothing> =
        Response.Error(cause, InternalServerError)
}
