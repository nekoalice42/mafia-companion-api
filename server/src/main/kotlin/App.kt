package me.nekoalice.mafia.api.server

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import me.nekoalice.mafia.api.dto.auth.AccessToken
import me.nekoalice.mafia.api.server.storage.StorageProvider
import me.nekoalice.mafia.api.server.storage.StorageType
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

private fun Application.getProperty(path: String) = environment.config.property(path)
private fun Application.getPropertyOrNull(path: String) = environment.config.propertyOrNull(path)

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
    )
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
