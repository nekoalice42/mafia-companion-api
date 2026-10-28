package me.nekoalice.mafia.api.dto.user.security

import kotlinx.serialization.Serializable

@Serializable
public data class InputPassword(
    val password: String,
)
