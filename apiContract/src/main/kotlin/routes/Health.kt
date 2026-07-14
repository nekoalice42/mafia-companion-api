package me.nekoalice.mafia.api.contracts.routes

import io.ktor.server.routing.*
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.contracts.openapi.descriptions.health.HealthDescriber
import me.nekoalice.mafia.api.contracts.resources.HealthResource
import me.nekoalice.mafia.api.contracts.routes.meta.defineRoute

context(routing: Route)
internal fun BaseAPI.applyPublicHealthRoutes() {
    routing.defineRoute<HealthResource, _>(Get, HealthDescriber) {
        getHealth()
    }
}
