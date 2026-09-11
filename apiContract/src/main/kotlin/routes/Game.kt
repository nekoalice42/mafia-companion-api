package me.nekoalice.mafia.api.contracts.routes

import io.ktor.server.routing.*
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.contracts.openapi.descriptions.game.GameByIdDescriber
import me.nekoalice.mafia.api.contracts.openapi.descriptions.tournament.TournamentGameDescriber
import me.nekoalice.mafia.api.contracts.resources.GameResource
import me.nekoalice.mafia.api.contracts.resources.TournamentResource
import me.nekoalice.mafia.api.contracts.routes.meta.defineRoute
import me.nekoalice.mafia.api.dto.game.Game

context(routing: Route)
internal fun BaseAPI.applyPrivateGameRoutes() {
    routing.defineRoute<TournamentResource.ById.Game, Game, _>(
        Post,
        TournamentGameDescriber,
    ) { res, game -> createGame(res.parent.id, game) }

    routing.defineRoute<GameResource.ById, _>(Delete, GameByIdDescriber) {
        deleteGame(it.id)
    }
}
