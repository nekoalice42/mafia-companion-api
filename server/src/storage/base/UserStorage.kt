package me.nekoalice.mafia.api.server.storage.base

import me.nekoalice.mafia.api.dto.user.InputUser
import me.nekoalice.mafia.api.dto.user.User
import me.nekoalice.mafia.api.dto.user.UserId
import me.nekoalice.mafia.api.dto.user.external.ExternalUserBind
import me.nekoalice.mafia.api.dto.user.external.ExternalUserProvider

interface UserStorage : CRUDStorage<User, InputUser, UserId> {
    suspend fun getByUsernameOrNull(username: String): User?
    suspend fun getByExternalIdOrNull(externalId: String, provider: ExternalUserProvider): User?
    suspend fun getAllExternalIds(id: UserId): List<ExternalUserBind>
    suspend fun setExternalUserId(id: UserId, provider: ExternalUserProvider, externalId: String)
    suspend fun unsetExternalUserId(id: UserId, provider: ExternalUserProvider)
}
