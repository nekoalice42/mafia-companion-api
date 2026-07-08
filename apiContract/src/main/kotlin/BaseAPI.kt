package me.nekoalice.mafia.api.contracts

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*
import me.nekoalice.mafia.api.contracts.routes.*
import me.nekoalice.mafia.api.dto.auth.*
import me.nekoalice.mafia.api.dto.game.NewGameBody
import me.nekoalice.mafia.api.dto.health.HealthResponse
import me.nekoalice.mafia.api.dto.health.HelloResponse
import me.nekoalice.mafia.api.dto.player.Player
import me.nekoalice.mafia.api.dto.player.PlayerId
import me.nekoalice.mafia.api.dto.response.ErrorResponse
import me.nekoalice.mafia.api.dto.response.ResponseList
import me.nekoalice.mafia.api.dto.tournament.Tournament
import me.nekoalice.mafia.api.dto.tournament.TournamentId
import me.nekoalice.mafia.api.dto.tournament.scoreboard.ScoreboardRow
import me.nekoalice.mafia.api.dto.user.User
import me.nekoalice.mafia.api.dto.user.UserId

public abstract class BaseAPI(
    public val info: APIInfo,
) {
    public abstract suspend fun getRoot(): Response<HelloResponse>
    public abstract suspend fun getHealth(): Response<HealthResponse>
    public abstract suspend fun login(loginData: LoginData): Response<TokenPair>
    public abstract suspend fun getMe(userId: UserId): Response<User>
    public abstract suspend fun changePassword(loginData: LoginData): Response<Unit>
    public abstract suspend fun refreshLogin(refreshToken: RefreshToken): Response<TokenPair>
    public abstract suspend fun logoutAll(userId: UserId): Response<Unit>
    public abstract suspend fun getPlayers(): Response<ResponseList<Player>>
    public abstract suspend fun getPlayer(playerId: PlayerId): Response<Player>
    public abstract suspend fun upsertPlayer(player: Player): Response<Unit>
    public abstract suspend fun deletePlayer(playerId: PlayerId): Response<Unit>
    public abstract suspend fun getTournaments(): Response<ResponseList<Tournament>>
    public abstract suspend fun getTournament(id: TournamentId): Response<Tournament>
    public abstract suspend fun upsertTournament(tournament: Tournament): Response<Unit>
    public abstract suspend fun deleteTournament(tournamentId: TournamentId): Response<Unit>
    public abstract suspend fun createGame(game: NewGameBody): Response<Unit>
    public abstract suspend fun getScoreboard(
        tournamentId: TournamentId,
    ): Response<ResponseList<ScoreboardRow>>

    public abstract suspend fun telegramOauthCallback(
        token: TelegramIdToken,
        oauthState: String,
    ): Response<ExternalAuthChallenge>

    public abstract suspend fun telegramOauthCallbackHtml(
        token: TelegramIdToken,
        oauthState: String,
    ): Response<Unit>

    public abstract suspend fun finishTelegramChallenge(code: ExternalAuthCode): Response<TokenPair>

    public abstract suspend fun handleAuthentication(token: AccessToken): UserId?
    public abstract suspend fun handleNewTelegramOauthState(
        oauthState: String,
        redirectUrl: String?,
        clientState: String?,
    )

    public abstract suspend fun handleTelegramOauthError(
        cause: AuthenticationFailedCause.Error,
    ): Response<Nothing>

    public fun applySecureRoutesTo(routing: Route): Unit = with(routing) {
        applyPrivateAuthRoutes()
        applyPrivateGameRoutes()
        applyPrivatePlayerRoutes()
        applyPrivateTournamentRoutes()
        applyPrivateUserRoutes()
    }

    public fun applyPublicRoutesTo(routing: Route): Unit = with(routing) {
        applyPublicAuthRoutes()
        applyPublicHealthRoutes()
        applyPublicOpenAPIJSONRoutes()
        applyPublicPlayerRoutes()
        applyPublicRootRoutes()
        applyPublicTournamentRoutes()
    }

    public fun applyTelegramOauthRoutesTo(routing: Route): Unit = with(routing) {
        applyTelegramOauthRoutes(isConfigured = true)
    }

    public fun applyUnavailableTelegramOauthRoutesTo(routing: Route): Unit = with(routing) {
        applyTelegramOauthRoutes(isConfigured = false)
    }

    public val requiredMethods: List<HttpMethod> = listOf(
        HttpMethod.Get,
        HttpMethod.Post,
        HttpMethod.Put,
        HttpMethod.Patch,
        HttpMethod.Delete,
    )

    public val requiredHeaders: List<String> = listOf(
        CustomHttpHeaders.XRefreshToken,
    )

    public sealed interface Response<SuccessT : Any> {
        public suspend fun sendInResponseTo(call: ApplicationCall)

        public data class Success<T : Any>(
            val response: T?,
            val statusCode: HttpStatusCode = HttpStatusCode.OK,
            private val typeInfo: TypeInfo?,
        ) : Response<T> {
            public constructor(
                statusCode: HttpStatusCode = HttpStatusCode.NoContent,
            ) : this(null, statusCode, null)

            init {
                require(
                    response != null && typeInfo != null
                            || response == null && typeInfo == null,
                ) { "Response=${response} and typeInfo=${typeInfo} must be both null or both non-null" }
            }

            private val isEmptyResponse = response == null

            override suspend fun sendInResponseTo(call: ApplicationCall): Unit =
                if (isEmptyResponse)
                    call.respond(statusCode)
                else
                    call.respond(statusCode, response!!, typeInfo!!)
        }

        public data class Raw<Unused : Any>(
            val body: String,
            val contentType: ContentType,
            val statusCode: HttpStatusCode = HttpStatusCode.OK,
        ) : Response<Unused> {
            override suspend fun sendInResponseTo(call: ApplicationCall): Unit =
                call.respondText(body, contentType, statusCode)

            public companion object {
                public fun <Unused : Any> html(
                    body: String,
                    statusCode: HttpStatusCode = HttpStatusCode.OK,
                ): Raw<Unused> = Raw(
                    body,
                    ContentType.Text.Html.withCharset(Charsets.UTF_8),
                    statusCode,
                )

                public fun <Unused : Any> text(
                    body: String,
                    statusCode: HttpStatusCode = HttpStatusCode.OK,
                ): Raw<Unused> = Raw(
                    body,
                    ContentType.Text.Plain.withCharset(Charsets.UTF_8),
                    statusCode,
                )

                public fun <Unused : Any> rawJson(
                    body: String,
                    statusCode: HttpStatusCode = HttpStatusCode.OK,
                ): Raw<Unused> = Raw(
                    body,
                    ContentType.Application.Json.withCharset(Charsets.UTF_8),
                    statusCode,
                )
            }
        }

        public data class Error<Unused : Any>(
            val message: String,
            val statusCode: HttpStatusCode = HttpStatusCode.InternalServerError,
        ) : Response<Unused> {
            public constructor(
                exception: Exception,
                statusCode: HttpStatusCode = HttpStatusCode.InternalServerError,
            ) : this(exception.message ?: "Unknown error", statusCode)

            override suspend fun sendInResponseTo(call: ApplicationCall): Unit =
                call.respond(statusCode, ErrorResponse(message))
        }

        public data class Redirect<Unused : Any>(
            val url: String,
        ) : Response<Unused> {
            override suspend fun sendInResponseTo(call: ApplicationCall): Unit =
                call.respondRedirect(url)
        }

        public companion object {
            public inline fun <reified T : Any> Success(
                response: T,
                statusCode: HttpStatusCode = HttpStatusCode.OK,
            ): Success<T> = Success(response, statusCode, typeInfo<T>())
        }
    }
}
