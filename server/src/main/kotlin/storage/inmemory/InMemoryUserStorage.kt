package me.nekoalice.mafia.api.server.storage.inmemory

import me.nekoalice.mafia.api.dto.models.User
import me.nekoalice.mafia.api.dto.models.UserId
import me.nekoalice.mafia.api.server.storage.adminUserUuid
import me.nekoalice.mafia.api.server.storage.base.UserStorage

class InMemoryUserStorage : UserStorage {
    private val users = mutableMapOf(
        UserId(adminUserUuid) to User(
            id = UserId(adminUserUuid),
            username = "admin",
        )
    )

    override suspend fun getByIdOrNull(id: UserId): User? =
        users[id]

    override suspend fun getByUsernameOrNull(username: String): User? =
        users.values.find { it.username == username }
}
