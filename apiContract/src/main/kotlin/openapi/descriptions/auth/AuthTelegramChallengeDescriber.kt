package me.nekoalice.mafia.api.contracts.openapi.descriptions.auth

import io.ktor.http.HttpMethod
import io.ktor.openapi.Operation
import me.nekoalice.mafia.api.contracts.openapi.descriptions.errorResponse
import me.nekoalice.mafia.api.contracts.openapi.descriptions.successResponseOf
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIResourceDescriber
import me.nekoalice.mafia.api.dto.auth.TokenPair

internal object AuthTelegramChallengeDescriber : OpenAPIResourceDescriber {
    override val supportedMethods: Set<HttpMethod> =
        setOf(Post)

    override fun describe(
        method: HttpMethod,
        builder: Operation.Builder,
    ) {
        require(method == Post) { "Only POST method is supported" }
        with(builder) {
            responses {
                successResponseOf<TokenPair>("Login successful")
                errorResponse(BadRequest, "No user found for auth code")
            }
        }
    }
}
