package me.nekoalice.mafia.api.dto.game

import kotlinx.serialization.Serializable

@Serializable
public data class ExtraPointsDescribed(
    val pointsX100: Int,
    val description: String,
)
