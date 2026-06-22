package me.nekoalice.mafia.api.contracts.resources

import io.ktor.resources.Resource
import me.nekoalice.mafia.api.dto.player.PlayerId
import kotlin.uuid.ExperimentalUuidApi

@Resource("/player")
internal class PlayerResource {
    @OptIn(ExperimentalUuidApi::class)
    @Resource("/{player_id}")
    class ById(
        val parent: PlayerResource,
        @Suppress("PropertyName") val player_id: PlayerId,
    )
}
