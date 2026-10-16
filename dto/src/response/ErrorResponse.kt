package me.nekoalice.mafia.api.dto.response

import kotlinx.serialization.Serializable

@Serializable
public data class ErrorResponse(val message: String)
