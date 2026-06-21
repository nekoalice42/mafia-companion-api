package me.nekoalice.mafia.api.server.storage.pg

import kotlinx.coroutines.flow.singleOrNull
import me.nekoalice.mafia.api.dao.Users
import me.nekoalice.mafia.api.dto.models.User
import me.nekoalice.mafia.api.dto.models.UserId
import me.nekoalice.mafia.api.server.storage.base.UserStorage
import org.jetbrains.exposed.v1.core.eq
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
}
