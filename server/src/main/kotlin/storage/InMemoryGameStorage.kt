package me.nekoalice.mafia.api.server.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import me.nekoalice.mafia.api.dto.models.NewGameBody
import me.nekoalice.mafia.api.server.storage.base.GameStorage

class InMemoryGameStorage : GameStorage {
    private val games = mutableListOf<NewGameBody>()

    override suspend fun create(game: NewGameBody) {
        games.add(game)
    }

    override fun getAll(): Flow<NewGameBody> = games.asFlow()
}
