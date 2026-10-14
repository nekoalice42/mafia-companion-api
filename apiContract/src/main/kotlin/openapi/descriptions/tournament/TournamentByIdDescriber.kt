package me.nekoalice.mafia.api.contracts.openapi.descriptions.tournament

import io.ktor.http.HttpMethod
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.openapi.Operation
import io.ktor.openapi.jsonSchema
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIResourceDescriber
import me.nekoalice.mafia.api.contracts.openapi.descriptions.defaultKtorBodyErrorResponsesFor
import me.nekoalice.mafia.api.contracts.openapi.descriptions.errorResponse
import me.nekoalice.mafia.api.contracts.openapi.descriptions.requestBodyOf
import me.nekoalice.mafia.api.contracts.openapi.descriptions.successResponse
import me.nekoalice.mafia.api.contracts.openapi.descriptions.successResponseOf
import me.nekoalice.mafia.api.contracts.openapi.descriptions.unsupportedMethod
import me.nekoalice.mafia.api.dto.tournament.InputTournament
import me.nekoalice.mafia.api.dto.tournament.Tournament
import me.nekoalice.mafia.api.dto.tournament.TournamentId

internal object TournamentByIdDescriber : OpenAPIResourceDescriber {
    override val supportedMethods: Set<HttpMethod> =
        setOf(Get, Put, Delete)

    override fun describe(
        method: HttpMethod,
        builder: Operation.Builder,
    ) = when (method) {
        Get -> describeGet(builder)
        Put -> describePut(builder)
        Delete -> describeDelete(builder)
        else -> unsupportedMethod(method)
    }

    context(builder: Operation.Builder)
    private fun tournamentIdParam(action: String) = builder.run {
        parameters {
            path("tournament_id") {
                required = true
                description = "Tournament ID to $action"
                schema = jsonSchema<TournamentId>()
            }
        }
    }

    private fun describeGet(builder: Operation.Builder) = builder.run {
        tournamentIdParam("retrieve")
        responses {
            successResponseOf<Tournament>("Tournament details")
            errorResponse(NotFound, "Tournament not found")
        }
    }

    private fun describePut(builder: Operation.Builder) = builder.run {
        tournamentIdParam("update")
        requestBodyOf<InputTournament>("Tournament details to update")
        responses {
            successResponse("Tournament updated", NoContent)
            errorResponse(NotFound, "Tournament not found")
            defaultKtorBodyErrorResponsesFor<InputTournament>()
        }
    }

    private fun describeDelete(builder: Operation.Builder) = builder.run {
        tournamentIdParam("delete")
        responses {
            successResponse("Tournament deleted or didn't exist before", NoContent)
        }
    }
}
