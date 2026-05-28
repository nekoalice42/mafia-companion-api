package me.nekoalice.mafia.api.contracts

public data class APIInfo(
    val name: String,
    val version: String,
    val licenseIdentifier: String?,
    val developmentUrl: String? = "http://localhost:8080",
    val productionUrl: String?,
)
