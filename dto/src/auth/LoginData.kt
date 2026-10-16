package me.nekoalice.mafia.api.dto.auth

import kotlinx.serialization.Serializable

@Serializable
public data class LoginData(
    val username: String,
    val password: String,
)
