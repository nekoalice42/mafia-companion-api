package me.nekoalice.mafia.api.contracts.openapi.descriptions.user

import io.ktor.http.HttpMethod
import io.ktor.openapi.Operation
import me.nekoalice.mafia.api.contracts.openapi.descriptions.successResponseOf
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIRouteDescriber
import me.nekoalice.mafia.api.dto.user.User

internal object UserMeDescriber : OpenAPIRouteDescriber {
    override val supportedMethods: Set<HttpMethod> =
        setOf(Get)

    override fun describe(
        method: HttpMethod,
        builder: Operation.Builder,
    ) {
        require(method == Get) { "Only GET method is supported" }
        with(builder) {
            responses {
                successResponseOf<User>("Logged in user info")
            }
        }
    }
}
