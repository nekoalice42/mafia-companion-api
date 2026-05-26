package me.nekoalice.mafia.api.models

import kotlinx.serialization.Serializable

@Serializable
data class ResponseList<T>(
    val items: List<T>,
    val count: Int = items.size,
) {
    init {
        require(items.size == count) { "item count mismatch" }
    }
}

@Serializable
data class ErrorResponse(val message: String)
