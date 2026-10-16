package me.nekoalice.mafia.api.server.modules

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.CannotTransformContentToTypeException
import io.ktor.server.plugins.statuspages.StatusPages
import me.nekoalice.mafia.api.server.utils.AttributeKeys

fun Application.configureStatusPages() {
    val api = attributes[AttributeKeys.api]

    install(StatusPages) {
        exception<BadRequestException> { call, cause ->
            api.onBadRequest(cause).sendInResponseTo(call)
        }
        exception<CannotTransformContentToTypeException> { call, cause ->
            api.onBadContentType(cause).sendInResponseTo(call)
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            api.onUnhandledException(cause).sendInResponseTo(call)
        }
    }
}
