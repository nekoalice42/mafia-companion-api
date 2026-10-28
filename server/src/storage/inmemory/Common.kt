package me.nekoalice.mafia.api.server.storage.inmemory

import me.nekoalice.mafia.api.dto.base.CRUDModel
import me.nekoalice.mafia.api.dto.base.InputCRUDModel
import me.nekoalice.mafia.api.dto.player.InputPlayer
import me.nekoalice.mafia.api.dto.player.Player
import me.nekoalice.mafia.api.dto.player.PlayerId
import me.nekoalice.mafia.api.dto.tournament.InputTournament
import me.nekoalice.mafia.api.dto.tournament.Tournament
import me.nekoalice.mafia.api.dto.tournament.TournamentId
import me.nekoalice.mafia.api.dto.user.InputUser
import me.nekoalice.mafia.api.dto.user.User
import me.nekoalice.mafia.api.dto.user.UserId
import me.nekoalice.mafia.api.server.storage.base.CRUDStorage

abstract class BaseMemoryStorage<
        ItemT : CRUDModel<IdT>,
        InputItemT : InputCRUDModel<ItemT, IdT>,
        IdT,
        >(
    private val idProvider: () -> IdT,
) : CRUDStorage<ItemT, InputItemT, IdT> {
    protected val items = mutableMapOf<IdT, ItemT>()

    override suspend fun add(item: InputItemT): IdT =
        idProvider().also { id -> items[id] = item.toModel(id) }

    override suspend fun edit(id: IdT, item: InputItemT) {
        if (items.containsKey(id)) items[id] = item.toModel(id)
    }

    override suspend fun getByIdOrNull(id: IdT): ItemT? = items[id]

    override suspend fun getAll(): List<ItemT> = items.values.toList()

    override suspend fun delete(id: IdT) {
        items.remove(id)
    }
}

internal typealias PlayerMemoryStorage = BaseMemoryStorage<Player, InputPlayer, PlayerId>
internal typealias TournamentMemoryStorage =
        BaseMemoryStorage<Tournament, InputTournament, TournamentId>

internal typealias UserMemoryStorage = BaseMemoryStorage<User, InputUser, UserId>
