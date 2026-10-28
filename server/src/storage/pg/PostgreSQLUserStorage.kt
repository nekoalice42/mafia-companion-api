package me.nekoalice.mafia.api.server.storage.pg

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import me.nekoalice.mafia.api.dao.ExternalUsers
import me.nekoalice.mafia.api.dao.Users
import me.nekoalice.mafia.api.dto.user.InputUser
import me.nekoalice.mafia.api.dto.user.User
import me.nekoalice.mafia.api.dto.user.UserId
import me.nekoalice.mafia.api.dto.user.external.ExternalUserBind
import me.nekoalice.mafia.api.dto.user.external.ExternalUserProvider
import me.nekoalice.mafia.api.server.storage.base.UserStorage
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.*

class PostgreSQLUserStorage : UserStorage {
    override suspend fun add(item: InputUser): UserId {
        val id = UserId.new()
        tx {
            Users.insert {
                it[this.id] = id.value
                it[username] = item.username
                it[passwordHash] = null
                it[role] = mapDtoUserRole(item.role)
            }
        }
        return id
    }

    override suspend fun getByIdOrNull(id: UserId): User? =
        readonlyTx {
            Users.selectAll()
                .where { Users.id eq id.value }
                .singleOrNull()
                ?.let(::userFromDao)
        }

    override suspend fun edit(id: UserId, item: InputUser) {
        tx {
            Users.update(where = { Users.id eq id.value }) {
                it[username] = item.username
                it[role] = mapDtoUserRole(item.role)
            }
        }
    }

    override suspend fun getAll(): List<User> = readonlyTx {
        Users.selectAll().map(::userFromDao).toList()
    }

    override suspend fun delete(id: UserId) {
        tx {
            Users.deleteWhere { Users.id eq id.value }
        }
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
        provider: ExternalUserProvider,
    ): User? = readonlyTx {
        (ExternalUsers innerJoin Users)
            .select(Users.columns)
            .where {
                (ExternalUsers.externalId eq externalId) and
                        (ExternalUsers.provider eq mapDtoProvider(provider))
            }.singleOrNull()
            ?.let(::userFromDao)
    }

    override suspend fun getAllExternalIds(id: UserId): List<ExternalUserBind> = readonlyTx {
        ExternalUsers.selectAll()
            .where { ExternalUsers.userId eq id.value }
            .map(::externalUserBindFromDao)
            .toList()
    }

    override suspend fun setExternalUserId(
        id: UserId,
        provider: ExternalUserProvider,
        externalId: String,
    ) {
        tx {
            ExternalUsers.upsert(ExternalUsers.userId, ExternalUsers.provider) {
                it[userId] = id.value
                it[this.externalId] = externalId
                it[this.provider] = mapDtoProvider(provider)
            }
        }
    }
}
