package me.nekoalice.mafia.api.dto.models

import kotlinx.serialization.Serializable

@Serializable
public data class ResponseList<T>(
    val items: List<T>,
    val count: Int = items.size,
) {
    init {
        require(items.size == count) { "item count mismatch" }
    }
}
