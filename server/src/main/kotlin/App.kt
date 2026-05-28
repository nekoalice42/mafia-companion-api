package me.nekoalice.mafia.api.server

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import me.nekoalice.mafia.api.server.storage.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val api = APIImpl(
        gameStorage = InMemoryGameStorage(),
        playerStorage = InMemoryPlayerStorage(),
    )
    install(ContentNegotiation) {
        json(
            Json(DefaultJson) {
                prettyPrint = true
            }
        )
    }
//    install(Resources)
    install(CORS) {
        anyHost()
        anyMethod()
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
    }
    routing {
        api.applyTo(this)
    }
}
