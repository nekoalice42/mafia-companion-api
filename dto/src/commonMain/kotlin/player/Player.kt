package me.nekoalice.mafia.api.dto.player

import kotlinx.serialization.Serializable

@Serializable
public data class Player(
    val id: PlayerId,
    val nickname: String,
)
