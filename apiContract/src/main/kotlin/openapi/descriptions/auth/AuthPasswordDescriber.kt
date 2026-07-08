package me.nekoalice.mafia.api.contracts.openapi.descriptions.auth

import io.ktor.http.HttpMethod
import io.ktor.openapi.Operation
import me.nekoalice.mafia.api.contracts.openapi.descriptions.defaultKtorBodyErrorResponsesFor
import me.nekoalice.mafia.api.contracts.openapi.descriptions.errorResponse
import me.nekoalice.mafia.api.contracts.openapi.descriptions.requestBodyOf
import me.nekoalice.mafia.api.contracts.openapi.descriptions.successResponse
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIRouteDescriber
import me.nekoalice.mafia.api.dto.auth.LoginData

internal object AuthPasswordDescriber : OpenAPIRouteDescriber {
    override val supportedMethods: Set<HttpMethod> =
        setOf(Put)

    override fun describe(
        method: HttpMethod,
        builder: Operation.Builder,
    ) {
        require(method == Put) { "Only PUT method is supported" }
        with(builder) {
            requestBodyOf<LoginData>("Username and new password of a user")
            responses {
                successResponse("Successfully changed password")
                errorResponse(
                    BadRequest,
                    "Request body could not be converted to LoginData, or specified user not found",
                )
                errorResponse(Unauthorized, "Invalid credentials")
                defaultKtorBodyErrorResponsesFor<LoginData>()
            }
        }
    }
}
