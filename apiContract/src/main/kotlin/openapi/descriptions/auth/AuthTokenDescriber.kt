package me.nekoalice.mafia.api.contracts.openapi.descriptions.auth

import io.ktor.http.HttpMethod
import io.ktor.openapi.Operation
import io.ktor.openapi.jsonSchema
import me.nekoalice.mafia.api.contracts.CustomHttpHeaders
import me.nekoalice.mafia.api.contracts.openapi.descriptions.defaultKtorBodyErrorResponsesFor
import me.nekoalice.mafia.api.contracts.openapi.descriptions.errorResponse
import me.nekoalice.mafia.api.contracts.openapi.descriptions.requestBodyOf
import me.nekoalice.mafia.api.contracts.openapi.descriptions.successResponse
import me.nekoalice.mafia.api.contracts.openapi.descriptions.successResponseOf
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIResourceDescriber
import me.nekoalice.mafia.api.dto.auth.LoginData
import me.nekoalice.mafia.api.dto.auth.RefreshToken
import me.nekoalice.mafia.api.dto.auth.TokenPair

internal object AuthTokenDescriber : OpenAPIResourceDescriber {
    override val supportedMethods: Set<HttpMethod> =
        setOf(Post, Patch, Delete)

    override fun describe(
        method: HttpMethod,
        builder: Operation.Builder,
    ) = when (method) {
        Post -> describePost(builder)
        Patch -> describePatch(builder)
        Delete -> describeDelete(builder)
        else -> throw IllegalArgumentException("Unsupported method: $method")
    }

    private fun describePost(builder: Operation.Builder) = builder.run {
        requestBodyOf<LoginData>("Login data")
        responses {
            successResponseOf<TokenPair>("Login successful")
            errorResponse(Unauthorized, "Invalid credentials")
            errorResponse(UnprocessableEntity, "Username and password validation failed")
            defaultKtorBodyErrorResponsesFor<LoginData>()
        }
    }

    private fun describePatch(builder: Operation.Builder) = builder.run {
        parameters {
            header(CustomHttpHeaders.XRefreshToken) {
                required = true
                description = "Refresh token"
                schema = jsonSchema<RefreshToken>()
            }
        }
        responses {
            successResponseOf<TokenPair>("New access and refresh tokens")
            errorResponse(Unauthorized, "Refresh token is missing or invalid")
        }
    }

    private fun describeDelete(builder: Operation.Builder) = builder.run {
        responses {
            successResponse("Successfully logged out")
            errorResponse(Unauthorized, "Token is invalid or not provided")
        }
    }

}
