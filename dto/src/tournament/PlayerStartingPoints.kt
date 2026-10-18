package me.nekoalice.mafia.api.dto.tournament

import kotlinx.serialization.Serializable
import me.nekoalice.mafia.api.dto.player.PlayerId

@Serializable
public data class PlayerStartingPoints(
    val playerId: PlayerId,
    val startingPointsX100: Int,
)
