package me.nekoalice.mafia.api.dto.user

import kotlinx.serialization.Serializable

@Serializable
public data class User(
    val id: UserId,
    val username: String,
)
