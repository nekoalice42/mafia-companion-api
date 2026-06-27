package me.nekoalice.mafia.api.contracts.resources

import io.ktor.resources.*
import me.nekoalice.mafia.api.dto.player.PlayerId

@Resource("/player")
public class PlayerResource {
    @Resource("/{player_id}")
    public class ById(
        public val parent: PlayerResource,
        @Suppress("PropertyName") public val player_id: PlayerId,
    )
}
