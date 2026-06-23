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
import me.nekoalice.mafia.api.server.storage.StorageType
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import kotlin.time.Duration.Companion.minutes
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ContentNegotiationClient

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

private fun Application.getProperty(path: String) = environment.config.property(path)
private fun Application.getPropertyOrNull(path: String) = environment.config.propertyOrNull(path)

private object AttributeKeys {
    val api = AttributeKey<BaseAPI>("API")
    val client = AttributeKey<HttpClient>("Client")
}

fun Application.module() {
    val storageType = getPropertyOrNull("mafia-api.storage.type")?.getString()
        ?.let(StorageType::parse)
        ?: StorageType.POSTGRESQL
    if (storageType == StorageType.POSTGRESQL) {
        R2dbcDatabase.connect(
            url = "r2dbc:" + getProperty("mafia-api.storage.postgresql.url").getString(),
            driver = "postgresql",
            user = getProperty("mafia-api.storage.postgresql.user").getString(),
            password = getProperty("mafia-api.storage.postgresql.password").getString(),
        )
    }
    val api = APIImpl(
        storages = StorageProvider(storageType),
        telegramOidcClientId = getProperty("mafia-api.telegram-oidc.client_id").getString(),
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

    val clientId = getProperty("mafia-api.telegram-oidc.client_id").getString()
    val clientSecret = getProperty("mafia-api.telegram-oidc.client_secret").getString()
    val redirectHost = getProperty("mafia-api.telegram-oidc.redirect_host").getString()
    val stateSecret = getProperty("mafia-api.telegram-oidc.state_secret").getString()

    authentication {
        oauth("telegram") {
            this@oauth.client = client
            urlProvider = {
                "$redirectHost/auth/telegram/callback"
            }
            settings = OAuthServerSettings.OAuth2ServerSettings(
                name = "telegram",
                authorizeUrl = "https://oauth.telegram.org/auth",
                accessTokenUrl = "https://oauth.telegram.org/token",
                clientId = clientId,
                clientSecret = clientSecret,
                defaultScopes = listOf("openid"),
                requestMethod = HttpMethod.Post,
                accessTokenRequiresBasicAuth = true,
                nonceManager = StatelessHmacNonceManager(
                    key = stateSecret.encodeToByteArray(),
                    timeoutMillis = 5.minutes.inWholeMilliseconds,
                ),
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
