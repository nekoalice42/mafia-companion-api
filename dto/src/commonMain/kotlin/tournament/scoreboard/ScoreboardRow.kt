package me.nekoalice.mafia.api.dto.tournament.scoreboard

import kotlinx.serialization.Serializable
import me.nekoalice.mafia.api.dto.player.Player

@Serializable
public data class ScoreboardRow(
    val place: UInt?,
    val player: Player,
    val playCount: RoleCounter,
    val winCount: RoleCounter,
    val gamePointsX100: Int,
    val extraPointsX100: Int,
    val ciPointsX100: Int,
    val bestTurnPointsX100: Int,
    val firstNightDeaths: Int,
) {
    val totalPointsX100: Int
        get() = gamePointsX100 + extraPointsX100 + ciPointsX100 + bestTurnPointsX100
}
