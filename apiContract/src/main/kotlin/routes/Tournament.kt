package me.nekoalice.mafia.api.contracts.routes

import io.ktor.server.routing.Route
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.contracts.openapi.descriptions.tournament.TournamentByIdDescriber
import me.nekoalice.mafia.api.contracts.openapi.descriptions.tournament.TournamentDescriber
import me.nekoalice.mafia.api.contracts.openapi.descriptions.tournament.TournamentScoreboardDescriber
import me.nekoalice.mafia.api.contracts.resources.TournamentResource
import me.nekoalice.mafia.api.contracts.routes.meta.define
import me.nekoalice.mafia.api.dto.tournament.Tournament

context(routing: Route)
internal fun BaseAPI.applyPublicTournamentRoutes() {
    routing.define<TournamentResource, _>(Get, TournamentDescriber) {
        getTournaments()
    }

    routing.define<TournamentResource.ById, _>(Get, TournamentByIdDescriber) {
        getTournament(it.tournament_id)
    }

    routing.define<TournamentResource.ById.Scoreboard, _>(Get, TournamentScoreboardDescriber) {
        getScoreboard(it.parent.tournament_id)
    }
}

context(routing: Route)
internal fun BaseAPI.applyPrivateTournamentRoutes() {
    routing.define<TournamentResource, Tournament, _>(Put, TournamentDescriber) {
        upsertTournament(it)
    }

    routing.define<TournamentResource.ById, _>(Delete, TournamentByIdDescriber) {
        deleteTournament(it.tournament_id)
    }
}
