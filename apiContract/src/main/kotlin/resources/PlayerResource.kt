package me.nekoalice.mafia.api.contracts.resources

import io.ktor.resources.Resource
import me.nekoalice.mafia.api.dto.models.PlayerId
import kotlin.uuid.ExperimentalUuidApi

@Resource("/player")
public class PlayerResource {
    @OptIn(ExperimentalUuidApi::class)
    @Resource("/{player_id}")
    public class ById(
        public val parent: PlayerResource,
        @Suppress("PropertyName") public val player_id: PlayerId,
    )
}
