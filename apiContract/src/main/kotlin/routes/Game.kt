package me.nekoalice.mafia.api.contracts.routes

import io.ktor.server.routing.*
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.contracts.openapi.descriptions.game.GameDescriber
import me.nekoalice.mafia.api.contracts.resources.GameResource
import me.nekoalice.mafia.api.contracts.routes.meta.defineRoute
import me.nekoalice.mafia.api.dto.game.NewGameBody

context(routing: Route)
internal fun BaseAPI.applyPrivateGameRoutes() {
    routing.defineRoute<GameResource, NewGameBody, _>(Post, GameDescriber) {
        createGame(it)
    }
}
