package me.nekoalice.mafia.api.contracts.resources

import io.ktor.resources.*
import me.nekoalice.mafia.api.dto.player.PlayerId

@Resource("/player")
internal class PlayerResource {
    @Resource("/{player_id}")
    class ById(
        val parent: PlayerResource,
        @Suppress("PropertyName") val player_id: PlayerId,
    )
}
