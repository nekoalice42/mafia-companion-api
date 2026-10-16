package me.nekoalice.mafia.api.contracts.openapi.descriptions.player

import io.ktor.http.HttpMethod
import io.ktor.openapi.Operation
import io.ktor.openapi.jsonSchema
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIResourceDescriber
import me.nekoalice.mafia.api.contracts.openapi.descriptions.defaultKtorBodyErrorResponsesFor
import me.nekoalice.mafia.api.contracts.openapi.descriptions.errorResponse
import me.nekoalice.mafia.api.contracts.openapi.descriptions.requestBodyOf
import me.nekoalice.mafia.api.contracts.openapi.descriptions.successResponse
import me.nekoalice.mafia.api.contracts.openapi.descriptions.successResponseOf
import me.nekoalice.mafia.api.contracts.openapi.descriptions.unsupportedMethod
import me.nekoalice.mafia.api.dto.player.InputPlayer
import me.nekoalice.mafia.api.dto.player.Player
import me.nekoalice.mafia.api.dto.player.PlayerId

internal object PlayerByIdDescriber : OpenAPIResourceDescriber {
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
    private fun playerIdParam(action: String) = builder.run {
        parameters {
            path("player_id") {
                required = true
                description = "Player ID to $action"
                schema = jsonSchema<PlayerId>()
            }
        }
    }

    private fun describeGet(builder: Operation.Builder) = builder.run {
        playerIdParam("retrieve")
        responses {
            successResponseOf<Player>("Player details")
            errorResponse(NotFound, "Player not found")
        }
    }

    private fun describePut(builder: Operation.Builder) = builder.run {
        playerIdParam("update")
        requestBodyOf<InputPlayer>("Player details to update")
        responses {
            successResponse("Player updated", NoContent)
            errorResponse(NotFound, "Player not found")
            defaultKtorBodyErrorResponsesFor<InputPlayer>()
        }
    }

    private fun describeDelete(builder: Operation.Builder) = builder.run {
        playerIdParam("delete")
        responses {
            successResponse("Player deleted or didn't exist before", NoContent)
        }
    }
}
