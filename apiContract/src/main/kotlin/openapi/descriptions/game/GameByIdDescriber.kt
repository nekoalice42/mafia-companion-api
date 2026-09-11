package me.nekoalice.mafia.api.contracts.openapi.descriptions.game

import io.ktor.http.HttpMethod
import io.ktor.openapi.Operation
import io.ktor.openapi.jsonSchema
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIResourceDescriber
import me.nekoalice.mafia.api.contracts.openapi.descriptions.*
import me.nekoalice.mafia.api.dto.game.GameId

internal object GameByIdDescriber : OpenAPIResourceDescriber {
    override val supportedMethods: Set<HttpMethod> =
        setOf(Delete)

    override fun describe(
        method: HttpMethod,
        builder: Operation.Builder,
    ) {
        if (method !in supportedMethods) unsupportedMethod(method)
        with(builder) {
            parameters {
                path("game_id") {
                    required = true
                    description = "Game ID to delete"
                    schema = jsonSchema<GameId>()
                }
            }
            responses {
                successResponse("Game deleted or didn't exist before", NoContent)
            }
        }
    }
}
