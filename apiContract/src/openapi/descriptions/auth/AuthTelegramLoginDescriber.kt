package me.nekoalice.mafia.api.contracts.openapi.descriptions.auth

import io.ktor.http.HttpMethod
import io.ktor.openapi.Operation
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIResourceDescriber
import me.nekoalice.mafia.api.contracts.openapi.descriptions.errorResponse
import me.nekoalice.mafia.api.contracts.openapi.descriptions.unsupportedMethod

internal object AuthTelegramLoginDescriber : OpenAPIResourceDescriber {
    override val supportedMethods: Set<HttpMethod> =
        setOf(Get)

    override fun describe(
        method: HttpMethod,
        builder: Operation.Builder,
    ) {
        if (method !in supportedMethods) unsupportedMethod(method)
        with(builder) {
            description = "Start Telegram login flow"
            responses {
                errorResponse(ServiceUnavailable, "Telegram OAuth2 is not available")
            }
        }
    }
}
