package me.nekoalice.mafia.api.contracts.openapi.descriptions.user

import io.ktor.http.HttpMethod
import io.ktor.openapi.Operation
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIResourceDescriber
import me.nekoalice.mafia.api.contracts.openapi.descriptions.successResponseOf
import me.nekoalice.mafia.api.contracts.openapi.descriptions.unsupportedMethod
import me.nekoalice.mafia.api.dto.user.User

internal object UserMeDescriber : OpenAPIResourceDescriber {
    override val supportedMethods: Set<HttpMethod> =
        setOf(Get)

    override fun describe(
        method: HttpMethod,
        builder: Operation.Builder,
    ) {
        if (method !in supportedMethods) unsupportedMethod(method)
        with(builder) {
            responses {
                successResponseOf<User>("Logged in user info")
            }
        }
    }
}
