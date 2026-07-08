package me.nekoalice.mafia.api.server

import io.ktor.client.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.dto.auth.AccessToken
import me.nekoalice.mafia.api.server.storage.StorageProvider
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import kotlin.time.Duration.Companion.minutes
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ContentNegotiationClient

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

private object AttributeKeys {
    val api = AttributeKey<BaseAPI>("API")
    val client = AttributeKey<HttpClient>("Client")
    val config = AttributeKey<MafiaAppConfig>("MafiaConfig")
}

fun Application.module() {
    val config = loadAppConfig(environment.config.property("mafia-api"))
    if (config.storage is StorageConfig.PostgreSQL) {
        with(config.storage.config) {
            R2dbcDatabase.connect(
                url = "r2dbc:$url",
                driver = "postgresql",
                user = user,
                password = password,
            )
        }
    }
    val api = APIImpl(
        storages = StorageProvider(config.storage.type),
        telegramOidcClientId = config.telegramOidc?.clientId,
    )
    val httpClient = HttpClient {
        install(ContentNegotiationClient) {
            json(
                Json(DefaultJson) {
                    ignoreUnknownKeys = true
                },
            )
        }
    }
    attributes[AttributeKeys.api] = api
    attributes[AttributeKeys.client] = httpClient
    attributes[AttributeKeys.config] = config

    install(ContentNegotiation) {
        json(
            Json(DefaultJson) {
                prettyPrint = true
            },
        )
    }
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        api.requiredHeaders.forEach { allowHeader(it) }
        api.requiredMethods.forEach { allowMethod(it) }
    }
    install(Resources)
    authentication {
        bearer("app-token") {
            realm = api.info.name
            authenticate { api.handleAuthentication(AccessToken(it.token)) }
        }
    }
    routing {
        api.applyPublicRoutesTo(this)
        authenticate("app-token") {
            api.applySecureRoutesTo(this)
        }
    }
}

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
