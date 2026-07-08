package me.nekoalice.mafia.api.server.modules

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import kotlinx.serialization.json.Json
import me.nekoalice.mafia.api.server.APIImpl
import me.nekoalice.mafia.api.server.loadAppConfig
import me.nekoalice.mafia.api.server.storage.StorageProvider
import me.nekoalice.mafia.api.server.utils.AttributeKeys

fun Application.initDependencies() {
    val config = loadAppConfig(environment.config.property("mafia-api"))
    val api = APIImpl(
        storages = StorageProvider(config.storage.type),
        telegramOidcClientId = config.telegramOidc?.clientId,
        urls = config.openapi.urls,
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
