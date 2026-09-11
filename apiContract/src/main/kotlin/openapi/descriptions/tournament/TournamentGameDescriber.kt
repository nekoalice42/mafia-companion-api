package me.nekoalice.mafia.api.contracts.openapi.descriptions.tournament

import io.ktor.http.HttpMethod
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.UnprocessableEntity
import io.ktor.openapi.Operation
import io.ktor.openapi.jsonSchema
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIResourceDescriber
import me.nekoalice.mafia.api.contracts.openapi.descriptions.*
import me.nekoalice.mafia.api.dto.game.Game
import me.nekoalice.mafia.api.dto.tournament.TournamentId

internal object TournamentGameDescriber : OpenAPIResourceDescriber {
    override val supportedMethods: Set<HttpMethod> =
        setOf(Post)

    override fun describe(
        method: HttpMethod,
        builder: Operation.Builder,
    ) {
        if (method !in supportedMethods) unsupportedMethod(method)
        with(builder) {
            parameters {
                path("tournament_id") {
                    required = true
                    description = "Game tournament ID"
                    schema = jsonSchema<TournamentId>()
                }
            }
            requestBodyOf<Game>("Game details to create")
            responses {
                successResponse("Game created successfully", Created)
                errorResponse(NotFound, "Tournament not found")
                errorResponse(UnprocessableEntity, "Error validating the game")
                defaultKtorBodyErrorResponsesFor<Game>()
            }
        }
    }
}
