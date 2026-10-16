package me.nekoalice.mafia.api.server.utils

import io.ktor.client.HttpClient
import io.ktor.util.AttributeKey
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.server.MafiaAppConfig

internal object AttributeKeys {
    val api = AttributeKey<BaseAPI>("API")
    val client = AttributeKey<HttpClient>("Client")
    val config = AttributeKey<MafiaAppConfig>("MafiaConfig")
}
