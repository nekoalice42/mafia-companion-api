package me.nekoalice.mafia.api.server.modules

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.util.*
import me.nekoalice.mafia.api.server.utils.AttributeKeys
import kotlin.time.Duration.Companion.minutes

fun Application.configureTelegramOidc() {
    val api = attributes[AttributeKeys.api]
    val client = attributes[AttributeKeys.client]
    val oidcConfig = attributes[AttributeKeys.config].telegramOidc

    if (oidcConfig == null) {
        log.warn("Login via Telegram is not configured")
        routing {
            api.applyUnavailableTelegramOauthRoutesTo(this)
        }
        return
    }

    authentication {
        oauth("telegram") {
            this@oauth.client = client
            urlProvider = {
                "${oidcConfig.redirectHost}/auth/telegram/callback"
            }
            settings = OAuthServerSettings.OAuth2ServerSettings(
                name = "telegram",
                authorizeUrl = "https://oauth.telegram.org/auth",
                accessTokenUrl = "https://oauth.telegram.org/token",
                clientId = oidcConfig.clientId,
                clientSecret = oidcConfig.clientSecret,
                defaultScopes = listOf("openid"),
                requestMethod = HttpMethod.Post,
                accessTokenRequiresBasicAuth = true,
                nonceManager = StatelessHmacNonceManager(
                    key = oidcConfig.stateSecret.encodeToByteArray(),
                    timeoutMillis = 5.minutes.inWholeMilliseconds,
                ),
                onStateCreated = { call, state ->
                    api.handleNewTelegramOauthState(
                        state,
                        call.request.queryParameters["redirect_url"],
                        call.request.queryParameters["state"],
                    )
                },
            )
            fallback = { cause ->
                api.handleTelegramOauthError(cause).sendInResponseTo(this)
            }

        }
    }

    routing {
        authenticate("telegram") {
            api.applyTelegramOauthRoutesTo(this)
        }
    }
}
