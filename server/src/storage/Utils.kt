package me.nekoalice.mafia.api.server.storage

import kotlin.uuid.Uuid

val adminUserUuid = Uuid.parse("019ee53f-bc60-7000-8000-000000000000")
const val adminUserDefaultPassword = "D3faultPassw0rd!"

data class TokenPair(
    val access: String,
    val refresh: String,
)
