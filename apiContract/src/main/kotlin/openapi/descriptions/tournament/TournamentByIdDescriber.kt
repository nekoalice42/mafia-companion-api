package me.nekoalice.mafia.api.contracts.openapi.descriptions.tournament

import io.ktor.http.HttpMethod
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.openapi.Operation
import io.ktor.openapi.jsonSchema
import me.nekoalice.mafia.api.contracts.openapi.descriptions.errorResponse
import me.nekoalice.mafia.api.contracts.openapi.descriptions.successResponse
import me.nekoalice.mafia.api.contracts.openapi.descriptions.successResponseOf
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIRouteDescriber
import me.nekoalice.mafia.api.dto.tournament.Tournament
import me.nekoalice.mafia.api.dto.tournament.TournamentId

internal object TournamentByIdDescriber : OpenAPIRouteDescriber {
    override val supportedMethods: Set<HttpMethod> =
        setOf(Get, Delete)

    override fun describe(
        method: HttpMethod,
        builder: Operation.Builder,
    ) = when (method) {
        Get -> describeGet(builder)
        Delete -> describeDelete(builder)
        else -> throw IllegalArgumentException("Unsupported method: $method")
    }

    private fun describeGet(builder: Operation.Builder) = builder.run {
        parameters {
            path("tournament_id") {
                required = true
                description = "Tournament ID to retrieve"
                schema = jsonSchema<TournamentId>()
            }
        }
        responses {
            successResponseOf<Tournament>("Tournament details")
            errorResponse(NotFound, "Tournament not found")
        }
    }

    private fun describeDelete(builder: Operation.Builder) = builder.run {
        parameters {
            path("tournament_id") {
                required = true
                description = "Tournament ID to delete"
                schema = jsonSchema<TournamentId>()
            }
        }
        responses {
            successResponse("Tournament deleted or didn't exist before", NoContent)
        }
    }
}
