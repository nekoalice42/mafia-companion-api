package me.nekoalice.mafia.api.server.storage.pg

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import me.nekoalice.mafia.api.dao.Players
import me.nekoalice.mafia.api.dto.player.Player
import me.nekoalice.mafia.api.dto.player.PlayerId
import me.nekoalice.mafia.api.server.storage.base.PlayerStorage
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.upsert
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class PostgreSQLPlayerStorage : PlayerStorage {
    override suspend fun getByIdOrNull(id: PlayerId): Player? = readonlyTx {
        Players.selectAll().where { Players.id eq id.value }.map(::playerFromDao).singleOrNull()
    }

    override suspend fun editOrAdd(
        id: PlayerId,
        item: Player,
    ) {
        tx {
            Players.upsert(Players.id) {
                it[Players.id] = id.value
                it[Players.nickname] = item.nickname
            }
        }
    }

    override suspend fun getAll(): List<Player> = readonlyTx {
        Players.selectAll().map(::playerFromDao).toList()
    }

    override suspend fun delete(id: PlayerId) {
        tx {
            Players.deleteWhere { Players.id eq id.value }
        }
    }
}
