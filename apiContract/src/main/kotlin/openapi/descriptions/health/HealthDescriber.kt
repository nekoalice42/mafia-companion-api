package me.nekoalice.mafia.api.contracts.openapi.descriptions.health

import io.ktor.http.HttpMethod
import io.ktor.openapi.Operation
import me.nekoalice.mafia.api.contracts.openapi.descriptions.responseOf
import me.nekoalice.mafia.api.contracts.openapi.descriptions.successResponseOf
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIResourceDescriber
import me.nekoalice.mafia.api.dto.health.HealthResponse

internal object HealthDescriber : OpenAPIResourceDescriber {
    override val supportedMethods: Set<HttpMethod> =
        setOf(Get)

    override fun describe(
        method: HttpMethod,
        builder: Operation.Builder,
    ) {
        require(method == Get) { "Only GET method is supported" }
        with(builder) {
            responses {
                successResponseOf<HealthResponse>("Health check succeeded")
                responseOf<HealthResponse>(ServiceUnavailable, "Health check failed")
            }
        }
    }
}
