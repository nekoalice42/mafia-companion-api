package me.nekoalice.mafia.api.dto.game

import kotlinx.serialization.Serializable
import me.nekoalice.mafia.api.dto.base.InputCRUDModel
import me.nekoalice.mafia.api.dto.game.enums.Team
import kotlin.time.Instant

@Serializable
public data class InputGame(
    val players: List<InGamePlayer>,
    val winnerTeam: Team?,
    val startTime: Instant,
) : InputCRUDModel<Game, GameId> {
    override fun toModel(id: GameId): Game = Game(
        id = id,
        players = players,
        winnerTeam = winnerTeam,
        startTime = startTime,
    )
}
