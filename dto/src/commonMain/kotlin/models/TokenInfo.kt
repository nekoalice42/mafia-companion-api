package me.nekoalice.mafia.api.dto.models

import kotlinx.serialization.Serializable

@Serializable
public data class TokenInfo(
    val value: String,
    val expiresInSeconds: ULong,
)
