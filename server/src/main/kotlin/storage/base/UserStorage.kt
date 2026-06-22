package me.nekoalice.mafia.api.server.storage.base

import me.nekoalice.mafia.api.dto.user.User
import me.nekoalice.mafia.api.dto.user.UserId

interface UserStorage {
    suspend fun getByIdOrNull(id: UserId): User?
    suspend fun getByUsernameOrNull(username: String): User?
}
