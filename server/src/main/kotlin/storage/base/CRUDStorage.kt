package me.nekoalice.mafia.api.server.storage.base

interface CRUDStorage<ItemT, IdT> {
    suspend fun getByIdOrNull(id: IdT): ItemT?
    suspend fun editOrAdd(id: IdT, item: ItemT)
    suspend fun getAll(): List<ItemT>
    suspend fun delete(id: IdT)
}
