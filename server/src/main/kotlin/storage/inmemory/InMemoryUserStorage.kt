package me.nekoalice.mafia.api.server.storage.inmemory

import me.nekoalice.mafia.api.dto.user.User
import me.nekoalice.mafia.api.dto.user.UserId
import me.nekoalice.mafia.api.server.storage.adminUserUuid
import me.nekoalice.mafia.api.server.storage.base.UserStorage

class InMemoryUserStorage : UserStorage {
    private val users = mutableMapOf(
        UserId(adminUserUuid) to User(
            id = UserId(adminUserUuid),
            username = "admin",
        ),
    )

    override suspend fun getByIdOrNull(id: UserId): User? =
        users[id]

    override suspend fun getByUsernameOrNull(username: String): User? =
        users.values.find { it.username == username }

    override suspend fun getByExternalIdOrNull(
        externalId: String,
        provider: UserStorage.ExternalUserProvider,
    ): User? = null  // No external users support here, sorry
}
