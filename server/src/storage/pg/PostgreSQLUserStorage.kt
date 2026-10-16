package me.nekoalice.mafia.api.server.storage.pg

import kotlinx.coroutines.flow.singleOrNull
import me.nekoalice.mafia.api.dao.ExternalUsers
import me.nekoalice.mafia.api.dao.Users
import me.nekoalice.mafia.api.dto.user.User
import me.nekoalice.mafia.api.dto.user.UserId
import me.nekoalice.mafia.api.server.storage.base.UserStorage
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll

class PostgreSQLUserStorage : UserStorage {
    override suspend fun getByIdOrNull(id: UserId): User? =
        readonlyTx {
            Users.selectAll()
                .where { Users.id eq id.value }
                .singleOrNull()
                ?.let(::userFromDao)
        }

    override suspend fun getByUsernameOrNull(username: String): User? =
        readonlyTx {
            Users.selectAll()
                .where { Users.username eq username }
                .singleOrNull()
                ?.let(::userFromDao)
        }

    override suspend fun getByExternalIdOrNull(
        externalId: String,
        provider: UserStorage.ExternalUserProvider,
    ): User? = readonlyTx {
        (ExternalUsers innerJoin Users)
            .select(Users.columns)
            .where {
                (ExternalUsers.externalId eq externalId)
                    .and(ExternalUsers.provider eq mapProvider(provider))
            }.singleOrNull()
            ?.let(::userFromDao)
    }
}
