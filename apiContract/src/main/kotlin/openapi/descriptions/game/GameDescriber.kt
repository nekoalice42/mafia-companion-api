package me.nekoalice.mafia.api.contracts.openapi.descriptions.game

import io.ktor.http.HttpMethod
import io.ktor.openapi.Operation
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIResourceDescriber
import me.nekoalice.mafia.api.contracts.openapi.descriptions.*
import me.nekoalice.mafia.api.dto.game.NewGameBody

internal object GameDescriber : OpenAPIResourceDescriber {
    override val supportedMethods: Set<HttpMethod> =
        setOf(Post)

    override fun describe(
        method: HttpMethod,
        builder: Operation.Builder,
    ) {
        if (method !in supportedMethods) unsupportedMethod(method)
        with(builder) {
            requestBodyOf<NewGameBody>("Game details to create")
            responses {
                successResponse("Game created successfully", Created)
                errorResponse(NotFound, "Tournament not found")
                errorResponse(UnprocessableEntity, "Error validating the game")
                defaultKtorBodyErrorResponsesFor<NewGameBody>()
            }
        }
    }

}
