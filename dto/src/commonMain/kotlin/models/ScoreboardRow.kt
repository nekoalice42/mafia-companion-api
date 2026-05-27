package me.nekoalice.mafia.api.dto.models

import kotlinx.serialization.Serializable

@Serializable
public data class ScoreboardRow(
    val player: Player,
    val playCount: RoleCounter,
    val winCount: RoleCounter,
    val gamePointsX100: Int,
    val ciPointsX100: Int,
    val bestTurnPointsX100: Int,
) {
    val totalPointsX100: Int
        get() = gamePointsX100 + ciPointsX100 + bestTurnPointsX100
}
