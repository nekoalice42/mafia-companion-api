package me.nekoalice.mafia.api.contracts.routes

import io.ktor.server.routing.Route
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.contracts.openapi.descriptions.root.RootDescriber
import me.nekoalice.mafia.api.contracts.resources.RootResource
import me.nekoalice.mafia.api.contracts.routes.meta.define

context(routing: Route)
internal fun BaseAPI.applyPublicRootRoutes() {
    routing.define<RootResource, _>(Get, RootDescriber) {
        getRoot()
    }
}
