package me.nekoalice.mafia.api.server.storage

import me.nekoalice.mafia.api.server.storage.base.GameStorage
import me.nekoalice.mafia.api.server.storage.base.PlayerStorage
import me.nekoalice.mafia.api.server.storage.base.TournamentStorage
import me.nekoalice.mafia.api.server.storage.inmemory.InMemoryGameStorage
import me.nekoalice.mafia.api.server.storage.inmemory.InMemoryPlayerStorage
import me.nekoalice.mafia.api.server.storage.inmemory.InMemoryTournamentStorage
import me.nekoalice.mafia.api.server.storage.pg.PostgreSQLGameStorage
import me.nekoalice.mafia.api.server.storage.pg.PostgreSQLPlayerStorage
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

fun GameStorage(type: StorageType): GameStorage = when (type) {
    StorageType.IN_MEMORY -> InMemoryGameStorage()
    StorageType.POSTGRESQL -> PostgreSQLGameStorage()
}

fun PlayerStorage(type: StorageType): PlayerStorage = when (type) {
    StorageType.IN_MEMORY -> InMemoryPlayerStorage()
    StorageType.POSTGRESQL -> PostgreSQLPlayerStorage()
}

fun TournamentStorage(type: StorageType): TournamentStorage = when (type) {
    StorageType.IN_MEMORY -> InMemoryTournamentStorage()
    StorageType.POSTGRESQL -> PostgreSQLTournamentStorage()
}
