package me.nekoalice.mafia.api.dto.auth

import kotlinx.serialization.Serializable

@Serializable
public data class ExternalAuthChallenge(
    val code: String,
    val state: String?,
)
