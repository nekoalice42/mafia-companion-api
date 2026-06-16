package me.nekoalice.mafia.api.server.storage

import me.nekoalice.mafia.api.server.storage.base.StorageProvider
import me.nekoalice.mafia.api.server.storage.inmemory.InMemoryGameStorage
import me.nekoalice.mafia.api.server.storage.inmemory.InMemoryPlayerStorage
import me.nekoalice.mafia.api.server.storage.inmemory.InMemoryStorageProvider
import me.nekoalice.mafia.api.server.storage.inmemory.InMemoryTournamentStorage
import me.nekoalice.mafia.api.server.storage.pg.PostgreSQLGameStorage
import me.nekoalice.mafia.api.server.storage.pg.PostgreSQLPlayerStorage
import me.nekoalice.mafia.api.server.storage.pg.PostgreSQLStorageProvider
import me.nekoalice.mafia.api.server.storage.pg.PostgreSQLTournamentStorage

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
         * - `"inmemory"` or `"in_memory"` for [IN_MEMORY]
         * - `"pg"` or `"postgresql"` for [POSTGRESQL]
         * @return The corresponding StorageType enum value if the input matches a known storage
         *  type.
         * @throws IllegalArgumentException if the input does not match any valid storage type.
         */
        fun parse(type: String): StorageType = when (type) {
            "inmemory", "in_memory" -> IN_MEMORY
            "pg", "postgresql" -> POSTGRESQL
            else -> throw IllegalArgumentException(
                "Invalid storage type: $type; must be one of: inmemory, in_memory, pg, postgresql"
            )
        }
    }
}

fun StorageProvider(type: StorageType): StorageProvider = when (type) {
    StorageType.IN_MEMORY -> InMemoryStorageProvider(
        game = InMemoryGameStorage(),
        player = InMemoryPlayerStorage(),
        tournament = InMemoryTournamentStorage()
    )

    StorageType.POSTGRESQL -> PostgreSQLStorageProvider(
        game = PostgreSQLGameStorage(),
        player = PostgreSQLPlayerStorage(),
        tournament = PostgreSQLTournamentStorage()
    )
}
