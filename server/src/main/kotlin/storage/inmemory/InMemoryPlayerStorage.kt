package me.nekoalice.mafia.api.server.storage.inmemory

import me.nekoalice.mafia.api.dto.player.Player
import me.nekoalice.mafia.api.dto.player.PlayerId
import me.nekoalice.mafia.api.server.storage.base.PlayerStorage

class InMemoryPlayerStorage : PlayerStorage {
    private val players = mutableMapOf<PlayerId, Player>()

    override suspend fun editOrAdd(id: PlayerId, item: Player) {
        players[id] = item
    }

    override suspend fun getByIdOrNull(id: PlayerId): Player? = players[id]

    override suspend fun getAll(): List<Player> = players.values.toList()

    override suspend fun delete(id: PlayerId) {
        players.remove(id)
    }
}
