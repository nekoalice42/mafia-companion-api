package me.nekoalice.mafia.api.dto.auth

import kotlinx.serialization.Serializable

@Serializable
public data class ExternalAuthCode(
    val code: String,
)
