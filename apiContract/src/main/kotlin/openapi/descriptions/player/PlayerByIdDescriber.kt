package me.nekoalice.mafia.api.contracts.openapi.descriptions.player

import io.ktor.http.HttpMethod
import io.ktor.openapi.Operation
import io.ktor.openapi.jsonSchema
import me.nekoalice.mafia.api.contracts.openapi.descriptions.errorResponse
import me.nekoalice.mafia.api.contracts.openapi.descriptions.successResponse
import me.nekoalice.mafia.api.contracts.openapi.descriptions.successResponseOf
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIResourceDescriber
import me.nekoalice.mafia.api.dto.player.Player
import me.nekoalice.mafia.api.dto.player.PlayerId

internal object PlayerByIdDescriber : OpenAPIResourceDescriber {
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
            path("player_id") {
                required = true
                description = "Player ID to retrieve"
                schema = jsonSchema<PlayerId>()
            }
        }
        responses {
            successResponseOf<Player>("Player details")
            errorResponse(NotFound, "Player not found")
        }
    }

    private fun describeDelete(builder: Operation.Builder) = builder.run {
        parameters {
            path("player_id") {
                required = true
                description = "Player ID to delete"
                schema = jsonSchema<PlayerId>()
            }
        }
        responses {
            successResponse("Player deleted or didn't exist before", NoContent)
        }
    }
}
