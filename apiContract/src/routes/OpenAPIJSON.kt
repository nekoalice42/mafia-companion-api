package me.nekoalice.mafia.api.contracts.routes

import io.ktor.openapi.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import me.nekoalice.mafia.api.contracts.BaseAPI.Response
import me.nekoalice.mafia.api.contracts.resources.OpenAPIJSONResource
import me.nekoalice.mafia.api.contracts.routes.meta.defineRoute

context(routing: Route)
internal fun applyPublicOpenAPIJSONRoutes() {
    routing.defineRoute<OpenAPIJSONResource, _>(Get, null) {
        Response.Error("Temporarily removed", Gone)
    }
}
