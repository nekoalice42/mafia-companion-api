package me.nekoalice.mafia.api.dto.base

public interface InputCRUDModel<ParentT : CRUDModel<IdT>, IdT> {
    public fun toModel(id: IdT): ParentT
}
