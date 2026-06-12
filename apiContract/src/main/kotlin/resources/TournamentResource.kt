package me.nekoalice.mafia.api.contracts.resources

import io.ktor.resources.Resource
import me.nekoalice.mafia.api.dto.models.TournamentId
import kotlin.uuid.ExperimentalUuidApi

@Resource("/tournament")
public open class TournamentResource {
    @OptIn(ExperimentalUuidApi::class)
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
