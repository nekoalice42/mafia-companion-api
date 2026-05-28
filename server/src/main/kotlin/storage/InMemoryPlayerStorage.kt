package me.nekoalice.mafia.api.server.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import me.nekoalice.mafia.api.dto.models.Player
import me.nekoalice.mafia.api.dto.models.PlayerId
import me.nekoalice.mafia.api.server.storage.base.PlayerStorage

class InMemoryPlayerStorage : PlayerStorage {
    private val players = mutableMapOf<PlayerId, Player>()

    override suspend fun add(player: Player) {
        players[player.id] = player
    }

    override suspend fun edit(id: PlayerId, player: Player) {
        players[id] = player
    }

    override suspend fun getByIdOrNull(id: PlayerId): Player? = players[id]

    override fun getAll(): Flow<Player> = players.values.asFlow()
}
