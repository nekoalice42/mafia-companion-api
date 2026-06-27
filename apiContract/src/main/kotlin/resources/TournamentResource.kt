package me.nekoalice.mafia.api.contracts.resources

import io.ktor.resources.*
import me.nekoalice.mafia.api.dto.tournament.TournamentId

@Resource("/tournament")
public class TournamentResource {
    @Resource("/{tournament_id}")
    public class ById(
        public val parent: TournamentResource,
        @Suppress("PropertyName") public val tournament_id: TournamentId,
    ) {
        @Resource("/scoreboard")
        public class Scoreboard(
            public val parent: ById,
        )
    }
}
