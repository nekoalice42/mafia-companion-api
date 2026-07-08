package me.nekoalice.mafia.api.server.modules

import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import me.nekoalice.mafia.api.server.utils.AttributeKeys

fun Application.configureRouting() {
    val api = attributes[AttributeKeys.api]

    install(Resources)
    routing {
        api.applyPublicRoutesTo(this)
    }
}
