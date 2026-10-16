package me.nekoalice.mafia.api.contracts.routes

import io.ktor.server.routing.*
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.contracts.openapi.descriptions.game.GameByIdDescriber
import me.nekoalice.mafia.api.contracts.resources.GameResource
import me.nekoalice.mafia.api.contracts.routes.meta.defineRoute

context(routing: Route)
internal fun BaseAPI.applyPrivateGameRoutes() {
    routing.defineRoute<GameResource.ById, _>(Delete, GameByIdDescriber) {
        deleteGame(it.id)
    }
}
