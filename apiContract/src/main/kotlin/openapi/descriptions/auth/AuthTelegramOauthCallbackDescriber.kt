package me.nekoalice.mafia.api.contracts.openapi.descriptions.auth

import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.openapi.Operation
import io.ktor.openapi.jsonSchema
import me.nekoalice.mafia.api.contracts.openapi.descriptions.errorResponse
import me.nekoalice.mafia.api.contracts.openapi.descriptions.successResponse
import me.nekoalice.mafia.api.contracts.openapi.descriptions.successResponseOf
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIRouteDescriber
import me.nekoalice.mafia.api.dto.auth.ExternalAuthChallenge

internal object AuthTelegramOauthCallbackDescriber : OpenAPIRouteDescriber {
    override val supportedMethods: Set<HttpMethod> =
        setOf(Get)

    override fun describe(
        method: HttpMethod,
        builder: Operation.Builder,
    ) {
        require(method == Get) { "Only GET method is supported" }
        with(builder) {
            responses {
                successResponseOf<ExternalAuthChallenge>("Login successful") {
                    ContentType.Text.Html {
                        schema = jsonSchema<String>()
                    }
                }
                successResponse("Login successful, redirecting to provided redirect_url", Found)
                errorResponse(BadRequest, "Bad redirect_url")
                errorResponse(Forbidden, "No user registered with this Telegram account")
                errorResponse(ServiceUnavailable, "Telegram login flow failed")
            }
        }

    }
}
