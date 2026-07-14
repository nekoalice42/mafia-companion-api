package me.nekoalice.mafia.api.contracts.routes

import io.ktor.server.routing.*
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.contracts.openapi.descriptions.root.RootDescriber
import me.nekoalice.mafia.api.contracts.resources.RootResource
import me.nekoalice.mafia.api.contracts.routes.meta.defineRoute

context(routing: Route)
internal fun BaseAPI.applyPublicRootRoutes() {
    routing.defineRoute<RootResource, _>(Get, RootDescriber) {
        getRoot()
    }
}
