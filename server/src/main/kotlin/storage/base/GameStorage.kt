package me.nekoalice.mafia.api.server.storage.base

import kotlinx.coroutines.flow.Flow
import me.nekoalice.mafia.api.dto.models.NewGameBody

interface GameStorage {
    suspend fun create(game: NewGameBody)
    fun getAll(): Flow<NewGameBody>
}
