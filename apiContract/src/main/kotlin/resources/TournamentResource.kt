package me.nekoalice.mafia.api.contracts.resources

import io.ktor.resources.Resource
import me.nekoalice.mafia.api.dto.tournament.TournamentId
import kotlin.uuid.ExperimentalUuidApi

@Resource("/tournament")
internal class TournamentResource {
    @OptIn(ExperimentalUuidApi::class)
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
