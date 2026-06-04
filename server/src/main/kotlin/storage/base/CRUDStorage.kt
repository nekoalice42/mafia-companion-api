package me.nekoalice.mafia.api.server.storage.base

import kotlinx.coroutines.flow.Flow

interface CRUDStorage<ItemT, IdT> {
    suspend fun getByIdOrNull(id: IdT): ItemT?
    suspend fun editOrAdd(id: IdT, item: ItemT)
    fun getAll(): Flow<ItemT>
    suspend fun delete(id: IdT)
}
