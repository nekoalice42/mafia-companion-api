package me.nekoalice.mafia.api.dto.models

import kotlinx.serialization.Serializable

@Serializable
public data class InGamePlayer(
    val playerId: PlayerId,
    val role: Role,
    val extraPoints: ExtraPointsDescribed?,
    val guessedMafiaCount: Int?,
)
