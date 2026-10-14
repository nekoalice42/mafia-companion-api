package me.nekoalice.mafia.api.contracts.routes

import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.server.routing.*
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.contracts.openapi.descriptions.player.PlayerByIdDescriber
import me.nekoalice.mafia.api.contracts.openapi.descriptions.player.PlayerDescriber
import me.nekoalice.mafia.api.contracts.resources.PlayerResource
import me.nekoalice.mafia.api.contracts.routes.meta.defineRoute
import me.nekoalice.mafia.api.dto.player.InputPlayer

context(routing: Route)
internal fun BaseAPI.applyPublicPlayerRoutes() {
    routing.defineRoute<PlayerResource, _>(Get, PlayerDescriber) {
        getPlayers()
    }

    routing.defineRoute<PlayerResource.ById, _>(Get, PlayerByIdDescriber) {
        getPlayer(it.id)
    }
}

context(routing: Route)
internal fun BaseAPI.applyPrivatePlayerRoutes() {
    routing.defineRoute<PlayerResource, InputPlayer, _>(Post, PlayerDescriber) {
        createPlayer(it)
    }

    routing.defineRoute<PlayerResource.ById, InputPlayer, _>(
        Put,
        PlayerByIdDescriber,
    ) { res, player -> updatePlayer(res.id, player) }

    routing.defineRoute<PlayerResource.ById, _>(Delete, PlayerByIdDescriber) {
        deletePlayer(it.id)
    }
}
