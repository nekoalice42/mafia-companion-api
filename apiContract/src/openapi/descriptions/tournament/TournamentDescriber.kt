package me.nekoalice.mafia.api.contracts.openapi.descriptions.tournament

import io.ktor.http.HttpMethod
import io.ktor.openapi.Operation
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIResourceDescriber
import me.nekoalice.mafia.api.contracts.openapi.descriptions.*
import me.nekoalice.mafia.api.dto.response.NewResourceId
import me.nekoalice.mafia.api.dto.response.ResponseList
import me.nekoalice.mafia.api.dto.tournament.InputTournament
import me.nekoalice.mafia.api.dto.tournament.Tournament

internal object TournamentDescriber : OpenAPIResourceDescriber {
    override val supportedMethods: Set<HttpMethod> =
        setOf(Get, Post)

    override fun describe(
        method: HttpMethod,
        builder: Operation.Builder,
    ) = when (method) {
        Get -> describeGet(builder)
        Post -> describePost(builder)
        else -> unsupportedMethod(method)
    }

    private fun describeGet(builder: Operation.Builder) = builder.run {
        responses {
            successResponseOf<ResponseList<Tournament>>("List of tournaments")
        }
    }

    private fun describePost(builder: Operation.Builder) = builder.run {
        requestBodyOf<InputTournament>("Tournament details to create")
        responses {
            successResponseOf<NewResourceId>("Tournament created", Created)
            defaultKtorBodyErrorResponsesFor<InputTournament>()
        }
    }
}
