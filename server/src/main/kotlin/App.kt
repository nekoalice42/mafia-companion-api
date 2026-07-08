package me.nekoalice.mafia.api.server

import io.ktor.server.application.Application
import me.nekoalice.mafia.api.server.modules.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    initDependencies()
    configureDatabase()
    configureContentNegotiation()
    configureCORS()
    configureRouting()
    configureSecurity()
    configureTelegramOidc()
}
