package me.nekoalice.mafia.api.contracts.routes

import io.ktor.server.routing.*
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.contracts.openapi.descriptions.player.PlayerByIdDescriber
import me.nekoalice.mafia.api.contracts.openapi.descriptions.player.PlayerDescriber
import me.nekoalice.mafia.api.contracts.resources.PlayerResource
import me.nekoalice.mafia.api.contracts.routes.meta.defineRoute
import me.nekoalice.mafia.api.dto.player.Player

context(routing: Route)
internal fun BaseAPI.applyPublicPlayerRoutes() {
    routing.defineRoute<PlayerResource, _>(Get, PlayerDescriber) {
        getPlayers()
    }

    routing.defineRoute<PlayerResource.ById, _>(Get, PlayerByIdDescriber) {
        getPlayer(it.player_id)
    }
}

context(routing: Route)
internal fun BaseAPI.applyPrivatePlayerRoutes() {
    routing.defineRoute<PlayerResource, Player, _>(Put, PlayerDescriber) {
        upsertPlayer(it)
    }

    routing.defineRoute<PlayerResource.ById, _>(Delete, PlayerByIdDescriber) {
        deletePlayer(it.player_id)
    }
}
