package me.nekoalice.mafia.api.contracts

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
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
        }.describe {
            responses {
                HttpStatusCode.OK {
                    content {
                        schema = jsonSchema<HelloResponse>()
                    }
                    description = "Health check (greeting)"
                }
            }
        }

        get("/player") {
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

        put<Player>("/player") {
            call.respond(upsertPlayer(it))
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

        get("/player/{player_id}") {
            val playerId = PlayerId(call.pathParameters["player_id"]!!)
            call.respond(getPlayer(playerId))
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

        delete("/player/{player_id}") {
            val playerId = PlayerId(call.pathParameters["player_id"]!!)
            call.respond(deletePlayer(playerId))
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

        get("/tournament") {
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

        get("/tournament/{tournament_id}") {
            val id = TournamentId(call.pathParameters["tournament_id"]!!)
            call.respond(getTournament(id))
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

        put<Tournament>("/tournament") {
            call.respond(upsertTournament(it))
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

        delete("/tournament/{tournament_id}") {
            val tournamentId = TournamentId(call.pathParameters["tournament_id"]!!)
            call.respond(deleteTournament(tournamentId))
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

        post<NewGameBody>("/game") {
            call.respond(createGame(it))
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

        get("/tournament/{tournament_id}/scoreboard") {
            val tournamentId = TournamentId(call.pathParameters["tournament_id"]!!)
            call.respond(getScoreboard(tournamentId))
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
        HttpMethod.Delete,
    )

    public val requiredHeaders: List<String> = listOf()

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
