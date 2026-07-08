package me.nekoalice.mafia.api.server.modules

import io.ktor.server.application.*
import me.nekoalice.mafia.api.server.StorageConfig
import me.nekoalice.mafia.api.server.utils.AttributeKeys
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase

fun Application.configureDatabase() {
    val config = attributes[AttributeKeys.config]

    if (config.storage is StorageConfig.PostgreSQL) {
        with(config.storage.config) {
            R2dbcDatabase.connect(
                url = "r2dbc:$url",
                driver = "postgresql",
                user = user,
                password = password,
            )
        }
    }
}
