package me.nekoalice.mafia.api.contracts

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.principal
import io.ktor.server.resources.*
import io.ktor.server.resources.patch
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.util.reflect.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json
import me.nekoalice.mafia.api.contracts.resources.*
import me.nekoalice.mafia.api.dto.auth.AccessToken
import me.nekoalice.mafia.api.dto.response.ErrorResponse
import me.nekoalice.mafia.api.dto.health.HealthResponse
import me.nekoalice.mafia.api.dto.health.HelloResponse
import me.nekoalice.mafia.api.dto.auth.LoginData
import me.nekoalice.mafia.api.dto.game.NewGameBody
import me.nekoalice.mafia.api.dto.player.Player
import me.nekoalice.mafia.api.dto.player.PlayerId
import me.nekoalice.mafia.api.dto.auth.RefreshToken
import me.nekoalice.mafia.api.dto.response.ResponseList
import me.nekoalice.mafia.api.dto.tournament.scoreboard.ScoreboardRow
import me.nekoalice.mafia.api.dto.auth.TokenPair
import me.nekoalice.mafia.api.dto.tournament.Tournament
import me.nekoalice.mafia.api.dto.tournament.TournamentId
import me.nekoalice.mafia.api.dto.user.UserId
import kotlin.uuid.ExperimentalUuidApi

public abstract class BaseAPI(
    private val info: APIInfo,
) {
    public abstract suspend fun getRoot(): Response<HelloResponse>
    public abstract suspend fun getHealth(): Response<HealthResponse>
    public abstract suspend fun login(loginData: LoginData): Response<TokenPair>
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

    public abstract suspend fun handleAuthentication(token: AccessToken): UserId?

    @OptIn(ExperimentalKtorApi::class)
    public fun applySecureRoutesTo(routing: Route): Unit = with(routing) {
        put<AuthResource.Password, LoginData> { _, loginData ->
            call.respond(changePassword(loginData))
        }.describe {
            responses {
                HttpStatusCode.NoContent {
                    description = "Successfully changed password"
                }
                HttpStatusCode.BadRequest {
                    content {
                        schema = jsonSchema<ErrorResponse>()
                    }
                    description = "User not found"
                }
                HttpStatusCode.Unauthorized {
                    description = "Invalid credentials"
                }
                commonKtorBodyErrorResponses("LoginData")
            }
        }

        delete<AuthResource.Token> {
            val userId = call.principal<UserId>()!!
            call.respond(logoutAll(userId))
        }.describe {
            responses {
                HttpStatusCode.NoContent {
                    description = "Successfully logged out"
                }
                HttpStatusCode.Unauthorized {
                    description = "Token is invalid or not provided"
                }
            }
        }

        put<PlayerResource, Player> { _, player ->
            call.respond(upsertPlayer(player))
        }.describe {
            requestBody {
                required = true
                content {
                    schema = jsonSchema<Player>()
                }
                description = "Player details to upsert"
            }
            responses {
                commonPutResponses("Player")
                commonKtorBodyErrorResponses("Player")
            }
        }

        delete<PlayerResource.ById> { res ->
            call.respond(deletePlayer(res.player_id))
        }.describe {
            parameters {
                path("player_id") {
                    required = true
                    description = "Player ID to delete"
                    schema = jsonSchema<PlayerId>()
                }
            }
            responses {
                commonDeleteResponses("Player")
            }
        }

        put<TournamentResource, Tournament> { _, tournament ->
            call.respond(upsertTournament(tournament))
        }.describe {
            requestBody {
                required = true
                content {
                    schema = jsonSchema<Tournament>()
                }
                description = "Tournament details to upsert"
            }
            responses {
                commonPutResponses("Tournament")
                commonKtorBodyErrorResponses("Tournament")
            }
        }

        delete<TournamentResource.ById> { res ->
            call.respond(deleteTournament(res.tournament_id))
        }.describe {
            parameters {
                path("tournament_id") {
                    required = true
                    description = "Tournament ID to delete"
                    schema = jsonSchema<TournamentId>()
                }
            }
            responses {
                commonDeleteResponses("Tournament")
            }
        }

        post<GameResource, NewGameBody> { _, game ->
            call.respond(createGame(game))
        }.describe {
            requestBody {
                required = true
                content {
                    schema = jsonSchema<NewGameBody>()
                }
                description = "Game details to create"
            }
            responses {
                HttpStatusCode.Created {
                    description = "Game created successfully"
                }
                HttpStatusCode.NotFound {
                    description = "Tournament not found"
                    content {
                        schema = jsonSchema<ErrorResponse>()
                    }
                }
                HttpStatusCode.UnprocessableEntity {
                    description = "Error validating the game"
                    content {
                        schema = jsonSchema<ErrorResponse>()
                    }
                }
                commonKtorBodyErrorResponses("NewGameBody")
            }
        }
    }

    @OptIn(ExperimentalKtorApi::class, ExperimentalUuidApi::class)
    public fun applyPublicRoutesTo(routing: Route): Unit = with(routing) {
        get<OpenAPIJSONResource> {
            val doc = OpenApiDoc(
                info = OpenApiInfo(
                    title = info.name,
                    version = info.version,
                    license = info.licenseIdentifier?.let {
                        OpenApiInfo.License(
                            name = it,
                            identifier = it,
                        )
                    },
                ),
                servers = listOfNotNull(
                    info.developmentUrl?.let {
                        Server(url = it, description = "Development server")
                    },
                    info.productionUrl?.let {
                        Server(url = it, description = "Production server")
                    },
                ),
            ) + call.application.routingRoot.descendants()
            val encodedDoc = openapiJsonConfig.encodeToString(doc)
            call.respondText(encodedDoc, ContentType.Application.Json)
        }.hide()

        get<RootResource> {
            call.respond(getRoot())
        }.describe {
            responses {
                HttpStatusCode.OK {
                    content {
                        schema = jsonSchema<HelloResponse>()
                    }
                    description = "Liveness check"
                }
            }
        }

        get<HealthResource> {
            call.respond(getHealth())
        }.describe {
            responses {
                HttpStatusCode.OK {
                    content {
                        schema = jsonSchema<HealthResponse>()
                    }
                    description = "Health check succeeded"
                }
                HttpStatusCode.ServiceUnavailable {
                    content {
                        schema = jsonSchema<HealthResponse>()
                    }
                    description = "Health check failed"
                }
            }
        }

        post<AuthResource.Token, LoginData> { _, loginData ->
            call.respond(login(loginData))
        }.describe {
            requestBody {
                required = true
                content {
                    schema = jsonSchema<LoginData>()
                }
                description = "Login data"
            }
            responses {
                HttpStatusCode.OK {
                    content {
                        schema = jsonSchema<TokenPair>()
                    }
                    description = "Login successful"
                }
                HttpStatusCode.Unauthorized {
                    content {
                        schema = jsonSchema<ErrorResponse>()
                    }
                    description = "Invalid credentials"
                }
                HttpStatusCode.UnprocessableEntity {
                    content {
                        schema = jsonSchema<ErrorResponse>()
                    }
                    description = "Username and password validation failed"
                }
                commonKtorBodyErrorResponses("LoginData")
            }
        }

        patch<AuthResource.Token> {
            val refreshToken = call.request.headers[CustomHttpHeaders.XRefreshToken]
            if (refreshToken == null) {
                call.respond(
                    Response.Error<Unit>(
                        message = "Refresh token is missing",
                        statusCode = HttpStatusCode.Unauthorized,
                    )
                )
                return@patch
            }
            call.respond(refreshLogin(RefreshToken(refreshToken)))
        }.describe {
            parameters {
                header(CustomHttpHeaders.XRefreshToken) {
                    required = true
                    description = "Refresh token"
                    schema = jsonSchema<RefreshToken>()
                }
            }
            responses {
                HttpStatusCode.OK {
                    content {
                        schema = jsonSchema<TokenPair>()
                    }
                    description = "New access and refresh tokens"
                }
                HttpStatusCode.Unauthorized {
                    content {
                        schema = jsonSchema<ErrorResponse>()
                    }
                    description = "Refresh token is missing or invalid"
                }
            }
        }

        get<PlayerResource> {
            call.respond(getPlayers())
        }.describe {
            responses {
                HttpStatusCode.OK {
                    content {
                        schema = jsonSchema<ResponseList<Player>>()
                    }
                    description = "List of players"
                }
            }
        }

        get<PlayerResource.ById> { res ->
            call.respond(getPlayer(res.player_id))
        }.describe {
            parameters {
                path("player_id") {
                    required = true
                    description = "Player ID to retrieve"
                    schema = jsonSchema<PlayerId>()
                }
            }
            responses {
                HttpStatusCode.OK {
                    content {
                        schema = jsonSchema<Player>()
                    }
                    description = "Player details"
                }
                HttpStatusCode.NotFound {
                    content {
                        schema = jsonSchema<ErrorResponse>()
                    }
                    description = "Player not found"
                }
            }
        }

        get<TournamentResource> {
            call.respond(getTournaments())
        }.describe {
            responses {
                HttpStatusCode.OK {
                    content {
                        schema = jsonSchema<ResponseList<Tournament>>()
                    }
                    description = "List of tournaments"
                }
            }
        }

        get<TournamentResource.ById> { res ->
            call.respond(getTournament(res.tournament_id))
        }.describe {
            parameters {
                path("tournament_id") {
                    required = true
                    description = "Tournament ID to retrieve"
                    schema = jsonSchema<TournamentId>()
                }
            }
            responses {
                HttpStatusCode.OK {
                    content {
                        schema = jsonSchema<Tournament>()
                    }
                    description = "Tournament details"
                }
                HttpStatusCode.NotFound {
                    content {
                        schema = jsonSchema<ErrorResponse>()
                    }
                    description = "Tournament not found"
                }
            }
        }

        get<TournamentResource.ById.Scoreboard> { res ->
            call.respond(getScoreboard(res.parent.tournament_id))
        }.describe {
            parameters {
                path("tournament_id") {
                    description = "Tournament ID to retrieve scoreboard for"
                    schema = jsonSchema<TournamentId>()
                }
            }
            responses {
                HttpStatusCode.OK {
                    description = "Tournament scoreboard"
                    content {
                        schema = jsonSchema<ResponseList<ScoreboardRow>>()
                    }
                }
                HttpStatusCode.NotFound {
                    description = "Tournament not found"
                    content {
                        schema = jsonSchema<ErrorResponse>()
                    }
                }
            }
        }
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
        public data class Success<T : Any>(
            val response: T?,
            val statusCode: HttpStatusCode = HttpStatusCode.OK,
            private val typeInfo: TypeInfo?,
        ) : Response<T> {
            public constructor(
                statusCode: HttpStatusCode = HttpStatusCode.OK,
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

        public suspend fun sendInResponseTo(call: ApplicationCall)

        public companion object {
            public inline fun <reified T : Any> Success(
                response: T,
                statusCode: HttpStatusCode = HttpStatusCode.OK,
            ): Success<T> = Success(response, statusCode, typeInfo<T>())
        }
    }

    private companion object {
        private val openapiJsonConfig = Json {
            encodeDefaults = false
        }
    }
}

private suspend fun ApplicationCall.respond(response: BaseAPI.Response<*>) =
    response.sendInResponseTo(this)

private fun Responses.Builder.commonPutResponses(what: String) {
    HttpStatusCode.Created {
        description = "$what created"
    }
    HttpStatusCode.NoContent {
        description = "$what updated"
    }
}

private fun Responses.Builder.commonDeleteResponses(what: String) {
    HttpStatusCode.NoContent {
        description = "$what deleted or didn't exist before"
    }
}

private fun Responses.Builder.commonKtorBodyErrorResponses(what: String) {
    HttpStatusCode.BadRequest {
        description = "Request body could not be converted to $what"
    }
    HttpStatusCode.UnsupportedMediaType {
        description = "Request body content type is unsupported"
    }
}
