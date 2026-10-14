package me.nekoalice.mafia.api.server.storage.base

import me.nekoalice.mafia.api.dto.base.CRUDModel
import me.nekoalice.mafia.api.dto.base.InputCRUDModel

interface CRUDStorage<ItemT : CRUDModel<IdT>, InputItemT : InputCRUDModel<ItemT, IdT>, IdT> {
    suspend fun add(item: InputItemT): IdT
    suspend fun getByIdOrNull(id: IdT): ItemT?
    suspend fun edit(id: IdT, item: InputItemT)
    suspend fun getAll(): List<ItemT>
    suspend fun delete(id: IdT)
}
