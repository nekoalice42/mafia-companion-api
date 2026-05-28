package me.nekoalice.mafia.api.server.storage.base

import kotlinx.coroutines.flow.Flow
import me.nekoalice.mafia.api.dto.models.Player
import me.nekoalice.mafia.api.dto.models.PlayerId

interface PlayerStorage {
    suspend fun add(player: Player)
    suspend fun getByIdOrNull(id: PlayerId): Player?
    suspend fun edit(id: PlayerId, player: Player)
    fun getAll(): Flow<Player>
}
