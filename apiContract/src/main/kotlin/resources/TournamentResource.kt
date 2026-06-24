package me.nekoalice.mafia.api.contracts.resources

import io.ktor.resources.*
import me.nekoalice.mafia.api.dto.tournament.TournamentId

@Resource("/tournament")
internal class TournamentResource {
    @Resource("/{tournament_id}")
    class ById(
        val parent: TournamentResource,
        @Suppress("PropertyName") val tournament_id: TournamentId,
    ) {
        @Resource("/scoreboard")
        class Scoreboard(
            val parent: ById,
        )
    }
}
