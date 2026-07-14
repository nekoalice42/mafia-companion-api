package me.nekoalice.mafia.api.contracts.openapi.descriptions.player

import io.ktor.http.HttpMethod
import io.ktor.openapi.Operation
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIResourceDescriber
import me.nekoalice.mafia.api.contracts.openapi.descriptions.*
import me.nekoalice.mafia.api.dto.player.Player
import me.nekoalice.mafia.api.dto.response.ResponseList

internal object PlayerDescriber : OpenAPIResourceDescriber {
    override val supportedMethods: Set<HttpMethod> =
        setOf(Get, Put)

    override fun describe(
        method: HttpMethod,
        builder: Operation.Builder,
    ) = when (method) {
        Get -> describeGet(builder)
        Put -> describePut(builder)
        else -> unsupportedMethod(method)
    }

    private fun describeGet(builder: Operation.Builder) = builder.run {
        responses {
            successResponseOf<ResponseList<Player>>("List of players")
        }
    }

    private fun describePut(builder: Operation.Builder) = builder.run {
        requestBodyOf<Player>("Player details to upsert")
        responses {
            successResponse("Player created", Created)
            successResponse("Player updated", NoContent)
            defaultKtorBodyErrorResponsesFor<Player>()
        }
    }
}
