package me.nekoalice.mafia.api.contracts.resources

import io.ktor.resources.Resource
import kotlinx.serialization.SerialName
import me.nekoalice.mafia.api.dto.game.GameId

@Resource("/game")
public class GameResource {
    @Resource("/{game_id}")
    public class ById(
        public val parent: GameResource,
        @SerialName("game_id") public val id: GameId,
    )
}
