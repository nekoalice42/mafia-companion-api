package me.nekoalice.mafia.api.server

import io.ktor.server.application.Application
import me.nekoalice.mafia.api.server.modules.*

fun Application.module() {
    initDependencies()
    configureDatabase()
    configureContentNegotiation()
    configureCORS()
    configureRouting()
    configureSecurity()
    configureTelegramOidc()
    configureStatusPages()
}
