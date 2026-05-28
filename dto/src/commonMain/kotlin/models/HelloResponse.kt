package me.nekoalice.mafia.api.dto.models

import kotlinx.serialization.Serializable

@Serializable
public data class HelloResponse(
    val message: String = "Hello, World!",
)
