package me.nekoalice.mafia.api.contracts

public data class APIInfo(
    val name: String,
    val version: String,
    val licenseIdentifier: String?,
    val urls: EnvironmentUrls,
)
