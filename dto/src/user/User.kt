package me.nekoalice.mafia.api.dto.user

import kotlinx.serialization.Serializable
import me.nekoalice.mafia.api.dto.base.CRUDModel

@Serializable
public data class User(
    override val id: UserId,
    val username: String,
    val role: UserRole,
) : CRUDModel<UserId>
