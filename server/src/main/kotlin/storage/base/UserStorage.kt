package me.nekoalice.mafia.api.server.storage.base

import me.nekoalice.mafia.api.dto.models.User
import me.nekoalice.mafia.api.dto.models.UserId

interface UserStorage {
    suspend fun getByIdOrNull(id: UserId): User?
    suspend fun getByUsernameOrNull(username: String): User?
}
