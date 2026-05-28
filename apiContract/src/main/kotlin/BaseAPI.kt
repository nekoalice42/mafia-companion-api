package me.nekoalice.mafia.api.contracts

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.util.reflect.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json
import me.nekoalice.mafia.api.dto.models.*

public abstract class BaseAPI(
    private val info: APIInfo,
) {
    public abstract suspend fun getRoot(): Response<HelloResponse>
    public abstract suspend fun getPlayers(): Response<ResponseList<Player>>
    public abstract suspend fun postPlayer(player: Player): Response<Unit>
    public abstract suspend fun createGame(body: NewGameBody): Response<Unit>
    public abstract suspend fun getScoreboard(): Response<ResponseList<ScoreboardRow>>

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

        post<Player>("/player") { newPlayer ->
            postPlayer(newPlayer)
            call.respond(HttpStatusCode.NoContent)
        } // TODO: `.describe {}`

        post<NewGameBody>("/game") { newGame ->
            createGame(newGame)
            call.respond(HttpStatusCode.NoContent)
        } // TODO: `.describe {}`

        get("/scoreboard") {
            call.respond(getScoreboard())
        } // TODO: `.describe {}`
    }

    public sealed interface Response<SuccessT> {
        public data class Success<T>(
            val response: T? = null,
            val statusCode: HttpStatusCode = HttpStatusCode.OK,
            private val typeInfo: TypeInfo? = null,
        ) : Response<T> {
            init {
                require(
                    response != null && typeInfo != null
                            || response == null && typeInfo == null
                ) { "Response and typeInfo must be both null or both non-null" }
            }

            private val isEmptyResponse = response == null

            override suspend fun unwrap(context: RoutingContext): Unit = if (isEmptyResponse)
                context.call.respond(statusCode)
            else
                context.call.respond(statusCode, response!!, typeInfo!!)
        }

        public data class Error<Unused>(
            val message: String,
            val statusCode: HttpStatusCode = HttpStatusCode.InternalServerError,
        ) : Response<Unused> {
            public constructor(
                exception: Exception,
                statusCode: HttpStatusCode = HttpStatusCode.InternalServerError,
            ) : this(exception.message ?: "Unknown error", statusCode)

            override suspend fun unwrap(context: RoutingContext): Unit =
                context.call.respond(statusCode, ErrorResponse(message))
        }

        public suspend fun unwrap(context: RoutingContext)

        public companion object {
            public inline fun <reified T> Success(
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
