package me.nekoalice.mafia.api.server.modules

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.DefaultJson
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import kotlinx.serialization.json.Json
import me.nekoalice.mafia.api.contracts.EnvironmentUrls
import me.nekoalice.mafia.api.server.APIImpl
import me.nekoalice.mafia.api.server.loadAppConfig
import me.nekoalice.mafia.api.server.storage.StorageProvider
import me.nekoalice.mafia.api.server.utils.AttributeKeys

fun Application.initDependencies() {
    val config = loadAppConfig(environment.config.property("mafia-api"))
    val api = APIImpl(
        storages = StorageProvider(config.storage.type),
        telegramOidcClientId = config.telegramOidc?.clientId,
        urls = with(config.openapi.urls) {
            EnvironmentUrls(
                development = dev,
                staging = stage,
                production = prod,
            )
        },
        expiryConfig = config.login.expiry,
        appRedirectRegex = config.login.appRedirectUrlRegex,
    )
    val httpClient = HttpClient {
        install(ContentNegotiation) {
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
}
