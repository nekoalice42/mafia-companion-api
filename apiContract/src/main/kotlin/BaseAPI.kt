package me.nekoalice.mafia.api.contracts

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.util.reflect.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json
import me.nekoalice.mafia.api.dto.models.*

private suspend fun ApplicationCall.respond(response: BaseAPI.Response<*>) =
    response.sendInResponseTo(this)

public abstract class BaseAPI(
    private val info: APIInfo,
) {
    public abstract suspend fun getRoot(): Response<HelloResponse>
    public abstract suspend fun getPlayers(): Response<ResponseList<Player>>
    //public abstract suspend fun getPlayer(playerId: PlayerId): Response<Player>
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

    @OptIn(ExperimentalKtorApi::class)
    public fun applyTo(routing: Routing): Unit = with(routing) {
        get("/openapi.json") {
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
                    }
                ),
            ) + call.application.routingRoot.descendants()
            val encodedDoc = openapiJsonConfig.encodeToString(doc)
            call.respondText(encodedDoc, ContentType.Application.Json)
        }.hide()

        get("/") {
            call.respond(getRoot())
        } // TODO: `.describe {}`

        get("/player") {
            call.respond(getPlayers())
        } // TODO: `.describe {}`

        put<Player>("/player") {
            call.respond(upsertPlayer(it))
        } // TODO: `.describe {}`

        delete("/player/{player_id}") {
            val playerId = PlayerId(call.pathParameters["player_id"]!!)
            call.respond(deletePlayer(playerId))
        } // TODO: `.describe {}`

        get("/tournament") {
            call.respond(getTournaments())
        } // TODO: `.describe {}`

        get("/tournament/{tournament_id}") {
            val id = TournamentId(call.pathParameters["tournament_id"]!!)
            call.respond(getTournament(id))
        } // TODO: `.describe {}`

        put<Tournament>("/tournament") {
            call.respond(upsertTournament(it))
        } // TODO: `.describe {}`

        delete("/tournament/{tournament_id}") {
            val tournamentId = TournamentId(call.pathParameters["tournament_id"]!!)
            call.respond(deleteTournament(tournamentId))
        } // TODO: `.describe {}`

        post<NewGameBody>("/game") {
            call.respond(createGame(it))
        } // TODO: `.describe {}`

        get("/tournament/{tournament_id}/scoreboard") {
            val tournamentId = TournamentId(call.pathParameters["tournament_id"]!!)
            call.respond(getScoreboard(tournamentId))
        } // TODO: `.describe {}`
    }

    public val requiredMethods: List<HttpMethod> = listOf(
        HttpMethod.Get,
        HttpMethod.Post,
        HttpMethod.Put,
        HttpMethod.Delete,
    )

    public val requiredHeaders: List<String> = listOf(
        HttpHeaders.Accept,
        HttpHeaders.ContentType,
    )

    public sealed interface Response<SuccessT : Any> {
        public data class Success<T : Any>(
            val response: T?,
            val statusCode: HttpStatusCode = HttpStatusCode.OK,
            private val typeInfo: TypeInfo?,
        ) : Response<T> {
            public constructor(
                statusCode: HttpStatusCode = HttpStatusCode.OK
            ) : this(null, statusCode, null)

            init {
                require(
                    response != null && typeInfo != null
                            || response == null && typeInfo == null
                ) { "Response=${response} and typeInfo=${typeInfo} must be both null or both non-null" }
            }

            private val isEmptyResponse = response == null

            override suspend fun sendInResponseTo(call: ApplicationCall): Unit = if (isEmptyResponse)
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
