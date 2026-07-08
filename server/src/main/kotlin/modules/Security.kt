package me.nekoalice.mafia.api.server.modules

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import me.nekoalice.mafia.api.dto.auth.AccessToken
import me.nekoalice.mafia.api.server.utils.AttributeKeys

fun Application.configureSecurity() {
    val api = attributes[AttributeKeys.api]

    authentication {
        bearer("app-token") {
            realm = api.info.name
            authenticate { api.handleAuthentication(AccessToken(it.token)) }
        }
    }
    routing {
        authenticate("app-token") {
            api.applySecureRoutesTo(this)
        }
    }
}
