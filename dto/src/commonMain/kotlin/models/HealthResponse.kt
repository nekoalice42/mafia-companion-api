package me.nekoalice.mafia.api.dto.models

import kotlinx.serialization.Serializable

@Serializable
public data class HealthResponse(
    val isDatabaseHealthy: Boolean,
) {
    public val status: Boolean =
        isDatabaseHealthy
}
