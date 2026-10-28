package me.nekoalice.mafia.api.server.storage.inmemory

import me.nekoalice.mafia.api.dto.user.User
import me.nekoalice.mafia.api.dto.user.UserId
import me.nekoalice.mafia.api.dto.user.external.ExternalUserBind
import me.nekoalice.mafia.api.dto.user.external.ExternalUserProvider
import me.nekoalice.mafia.api.server.storage.adminUserUuid
import me.nekoalice.mafia.api.server.storage.base.UserStorage

class InMemoryUserStorage : UserStorage, UserMemoryStorage(UserId::new) {
    private val externalUsers = mutableMapOf<ExternalUserBind, UserId>()

    init {
        items[UserId(adminUserUuid)] = User(
            id = UserId(adminUserUuid),
            username = "admin",
            role = Admin,
        )
    }

    override suspend fun getByUsernameOrNull(username: String): User? =
        items.values.find { it.username == username }

    override suspend fun getByExternalIdOrNull(
        externalId: String,
        provider: ExternalUserProvider,
    ): User? = externalUsers[ExternalUserBind(provider, externalId)]
        ?.let { userId -> items[userId] }

    override suspend fun getAllExternalIds(id: UserId): List<ExternalUserBind> =
        externalUsers.filterValues { it == id }.keys.toList()

    override suspend fun setExternalUserId(
        id: UserId,
        provider: ExternalUserProvider,
        externalId: String,
    ) {
        unsetExternalUserId(id, provider)
        externalUsers[ExternalUserBind(provider, externalId)] = id
    }

    override suspend fun unsetExternalUserId(id: UserId, provider: ExternalUserProvider) {
        externalUsers.filter { (bind, userId) -> userId == id && bind.provider == provider }
            .keys
            .forEach { externalUsers.remove(it) }
    }
}
