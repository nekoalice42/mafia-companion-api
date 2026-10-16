package me.nekoalice.mafia.api.dto.auth

import kotlinx.serialization.Serializable

@Serializable
public data class TokenPair(
    val access: TokenInfo,
    val refresh: TokenInfo,
)
