package me.nekoalice.mafia.api.server

import io.ktor.server.config.ApplicationConfigValue
import io.ktor.server.config.getAs
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.nekoalice.mafia.api.server.storage.StorageType

sealed class StorageConfig(val type: StorageType) {
    data object InMemory : StorageConfig(StorageType.IN_MEMORY)

    data class PostgreSQL(
        val config: ConnectionConfig,
    ) : StorageConfig(StorageType.POSTGRESQL) {
        data class ConnectionConfig(
            val url: String,
            val user: String,
            val password: String,
        )
    }
}

data class TelegramOidcConfig(
    val clientId: String,
    val clientSecret: String,
    val redirectHost: String,
    val stateSecret: String,
)

data class OpenAPIConfig(
    val urls: EnvironmentUrls,
)

data class EnvironmentUrls(
    val dev: String,
    val stage: String?,
    val prod: String?,
)

data class MafiaAppConfig(
    val storage: StorageConfig,
    val telegramOidc: TelegramOidcConfig?,
    val openapi: OpenAPIConfig,
)

fun loadAppConfig(config: ApplicationConfigValue) = config.getAs<RawMafiaAppConfig>().let { raw ->
    MafiaAppConfig(
        storage = when (StorageType.parse(raw.storage.type)) {
            IN_MEMORY -> StorageConfig.InMemory
            POSTGRESQL -> with(raw.storage.postgresql) {
                StorageConfig.PostgreSQL(
                    config = StorageConfig.PostgreSQL.ConnectionConfig(
                        url = url!!,
                        user = user!!,
                        password = password!!,
                    )
                )
            }
        },
        telegramOidc = if (raw.telegramOidc.clientId != null)
            with(raw.telegramOidc) {
                TelegramOidcConfig(
                    clientId = clientId!!,
                    clientSecret = clientSecret!!,
                    redirectHost = redirectHost!!,
                    stateSecret = stateSecret!!,
                )
            }
        else null,
        openapi = with(raw.openapi) {
            OpenAPIConfig(
                urls = EnvironmentUrls(
                    dev = urls["dev"]!!,
                    stage = urls["stage"],
                    prod = urls["prod"],
                ),
            )
        },
    )
}

@Serializable
private data class RawMafiaAppConfig(
    val storage: RawStorageConfig,
    @SerialName("telegram-oidc")
    val telegramOidc: RawTelegramOidcConfig,
    val openapi: RawOpenAPIConfig,
) {
    @Serializable
    data class RawStorageConfig(
        val type: String,
        val postgresql: RawConnectionConfig,
    ) {
        @Serializable
        data class RawConnectionConfig(
            val url: String? = null,
            val user: String? = null,
            val password: String? = null,
        ) {
            init {
                require(
                    listOfNotNull(
                        url,
                        user,
                        password,
                    ).size in setOf(0, 3)
                ) { "PostgreSQL connection configuration must be either empty or fully specified" }
            }
        }

        init {
            if (StorageType.parse(type) == POSTGRESQL) {
                require(postgresql.url != null) {
                    "PostgreSQL connection data must be specified when storage type is 'postgresql'"
                }
            }
        }
    }

    @Serializable
    data class RawTelegramOidcConfig(
        @SerialName("client_id")
        val clientId: String? = null,
        @SerialName("client_secret")
        val clientSecret: String? = null,
        @SerialName("redirect_host")
        val redirectHost: String? = null,
        @SerialName("state_secret")
        val stateSecret: String? = null,
    ) {
        init {
            require(
                listOfNotNull(
                    clientId,
                    clientSecret,
                    redirectHost,
                    stateSecret,
                ).size in setOf(0, 4)
            ) { "Telegram OIDC configuration must be either empty or fully specified" }
        }
    }

    @Serializable
    data class RawOpenAPIConfig(
        val urls: Map<String, String?>
    ) {
        init {
            requireNotNull(urls["dev"]) { "'dev' URL must be specified" }
        }
    }
}
