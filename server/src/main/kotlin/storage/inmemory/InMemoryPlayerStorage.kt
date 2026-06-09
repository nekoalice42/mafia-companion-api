package me.nekoalice.mafia.api.server.storage.inmemory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import me.nekoalice.mafia.api.dto.models.Player
import me.nekoalice.mafia.api.dto.models.PlayerId
import me.nekoalice.mafia.api.server.storage.base.PlayerStorage

class InMemoryPlayerStorage : PlayerStorage {
    private val players = mutableMapOf<PlayerId, Player>()

    override suspend fun editOrAdd(id: PlayerId, item: Player) {
        players[id] = item
    }

    override suspend fun getByIdOrNull(id: PlayerId): Player? = players[id]

    override fun getAll(): Flow<Player> = players.values.asFlow()

    override suspend fun delete(id: PlayerId) {
        players.remove(id)
    }
}
