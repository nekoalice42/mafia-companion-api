package me.nekoalice.mafia.api.contracts.openapi.descriptions.tournament

import io.ktor.http.HttpMethod
import io.ktor.openapi.Operation
import me.nekoalice.mafia.api.contracts.openapi.descriptions.defaultKtorBodyErrorResponsesFor
import me.nekoalice.mafia.api.contracts.openapi.descriptions.requestBodyOf
import me.nekoalice.mafia.api.contracts.openapi.descriptions.successResponse
import me.nekoalice.mafia.api.contracts.openapi.descriptions.successResponseOf
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIRouteDescriber
import me.nekoalice.mafia.api.dto.response.ResponseList
import me.nekoalice.mafia.api.dto.tournament.Tournament

internal object TournamentDescriber : OpenAPIRouteDescriber {
    override val supportedMethods: Set<HttpMethod> =
        setOf(Get, Put)

    override fun describe(
        method: HttpMethod,
        builder: Operation.Builder,
    ) = when (method) {
        Get -> describeGet(builder)
        Put -> describePut(builder)
        else -> throw IllegalArgumentException("Unsupported method: $method")
    }

    private fun describeGet(builder: Operation.Builder) = builder.run {
        responses {
            successResponseOf<ResponseList<Tournament>>("List of tournaments")
        }
    }

    private fun describePut(builder: Operation.Builder) = builder.run {
        requestBodyOf<Tournament>("Tournament details to upsert")
        responses {
            successResponse("Tournament created", Created)
            successResponse("Tournament updated", NoContent)
            defaultKtorBodyErrorResponsesFor<Tournament>()
        }
    }
}
