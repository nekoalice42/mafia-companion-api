package me.nekoalice.mafia.api.contracts.openapi.descriptions.auth

import io.ktor.http.HttpMethod
import io.ktor.openapi.Operation
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIResourceDescriber
import me.nekoalice.mafia.api.contracts.openapi.descriptions.errorResponse

internal object AuthTelegramLoginDescriber : OpenAPIResourceDescriber {
    override val supportedMethods: Set<HttpMethod> =
        setOf(Get)

    override fun describe(
        method: HttpMethod,
        builder: Operation.Builder,
    ) {
        require(method == Get) { "Only GET method is supported" }
        with(builder) {
            description = "Start Telegram login flow"
            responses {
                errorResponse(ServiceUnavailable, "Telegram OAuth2 is not available")
            }
        }
    }
}
