package me.nekoalice.mafia.api.server.storage.inmemory

import me.nekoalice.mafia.api.dto.player.InputPlayer
import me.nekoalice.mafia.api.dto.player.Player
import me.nekoalice.mafia.api.dto.player.PlayerId
import me.nekoalice.mafia.api.server.storage.base.PlayerStorage

class InMemoryPlayerStorage : PlayerStorage {
    private val players = mutableMapOf<PlayerId, Player>()

    override suspend fun add(item: InputPlayer): PlayerId =
        PlayerId.new().also { id -> players[id] = item.toModel(id) }

    override suspend fun edit(id: PlayerId, item: InputPlayer) {
        if (players.containsKey(id)) players[id] = item.toModel(id)
    }

    override suspend fun getByIdOrNull(id: PlayerId): Player? = players[id]

    override suspend fun getAll(): List<Player> = players.values.toList()

    override suspend fun delete(id: PlayerId) {
        players.remove(id)
    }
}
