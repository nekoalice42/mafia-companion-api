package me.nekoalice.mafia.api.contracts.routes

import io.ktor.server.routing.Route
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.contracts.openapi.descriptions.player.PlayerByIdDescriber
import me.nekoalice.mafia.api.contracts.openapi.descriptions.player.PlayerDescriber
import me.nekoalice.mafia.api.contracts.resources.PlayerResource
import me.nekoalice.mafia.api.contracts.routes.meta.define
import me.nekoalice.mafia.api.dto.player.Player

context(routing: Route)
internal fun BaseAPI.applyPublicPlayerRoutes() {
    routing.define<PlayerResource, _>(Get, PlayerDescriber) {
        getPlayers()
    }

    routing.define<PlayerResource.ById, _>(Get, PlayerByIdDescriber) {
        getPlayer(it.player_id)
    }
}

context(routing: Route)
internal fun BaseAPI.applyPrivatePlayerRoutes() {
    routing.define<PlayerResource, Player, _>(Put, PlayerDescriber) {
        upsertPlayer(it)
    }

    routing.define<PlayerResource.ById, _>(Delete, PlayerByIdDescriber) {
        deletePlayer(it.player_id)
    }
}
