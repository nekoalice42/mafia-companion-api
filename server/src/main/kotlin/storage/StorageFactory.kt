package me.nekoalice.mafia.api.server.storage

import me.nekoalice.mafia.api.server.storage.base.StorageProvider
import me.nekoalice.mafia.api.server.storage.inmemory.*
import me.nekoalice.mafia.api.server.storage.pg.*

enum class StorageType {
    IN_MEMORY,
    POSTGRESQL,
    ;

    companion object {
        /**
         * Parses the given string representation of a storage type and returns the corresponding
         *  StorageType enum value.
         *
         * @param type The string representation of the storage type. Supported values are:
         * - `"in-memory"` for [IN_MEMORY]
         * - `"postgresql"` for [POSTGRESQL]
         * @return The corresponding StorageType enum value if the input matches a known storage
         *  type.
         * @throws IllegalArgumentException if the input does not match any valid storage type.
         */
        fun parse(type: String): StorageType = when (type) {
            "in-memory" -> IN_MEMORY
            "postgresql" -> POSTGRESQL
            else -> throw IllegalArgumentException(
                "Invalid storage type: $type; must be one of: 'in-memory', 'postgresql'",
            )
        }
    }
}

fun StorageProvider(type: StorageType): StorageProvider = when (type) {
    IN_MEMORY -> InMemoryStorageProvider(
        auth = InMemoryAuthStorage(),
        game = InMemoryGameStorage(),
        player = InMemoryPlayerStorage(),
        tournament = InMemoryTournamentStorage(),
        user = InMemoryUserStorage(),
    )

    POSTGRESQL -> PostgreSQLStorageProvider(
        auth = PostgreSQLAuthStorage(),
        game = PostgreSQLGameStorage(),
        player = PostgreSQLPlayerStorage(),
        tournament = PostgreSQLTournamentStorage(),
        user = PostgreSQLUserStorage(),
    )
}
