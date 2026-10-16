package me.nekoalice.mafia.api.server.modules

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import me.nekoalice.mafia.api.server.utils.AttributeKeys

fun Application.configureCORS() {
    val api = attributes[AttributeKeys.api]

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        api.requiredHeaders.forEach { allowHeader(it) }
        api.requiredMethods.forEach { allowMethod(it) }
    }
}
