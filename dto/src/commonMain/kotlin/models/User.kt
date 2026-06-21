package me.nekoalice.mafia.api.dto.models

import kotlinx.serialization.Serializable

@Serializable
public data class User(
    val id: UserId,
    val username: String,
)
