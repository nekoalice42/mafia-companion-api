package me.nekoalice.mafia.api.contracts.resources

import io.ktor.resources.*
import kotlinx.serialization.SerialName
import me.nekoalice.mafia.api.dto.tournament.TournamentId

@Resource("/tournament")
public class TournamentResource {
    @Resource("/{tournament_id}")
    public class ById(
        public val parent: TournamentResource,
        @SerialName("tournament_id") public val id: TournamentId,
    ) {
        @Resource("/scoreboard")
        public class Scoreboard(
            public val parent: ById,
        )

        @Resource("/game")
        public class Game(
            public val parent: ById,
        )
    }
}
