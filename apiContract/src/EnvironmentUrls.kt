package me.nekoalice.mafia.api.contracts

public data class EnvironmentUrls(
    val development: String? = "http://localhost:8080",
    val staging: String? = null,
    val production: String?,
)
