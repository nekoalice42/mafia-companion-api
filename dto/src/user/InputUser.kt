package me.nekoalice.mafia.api.dto.user

import kotlinx.serialization.Serializable
import me.nekoalice.mafia.api.dto.base.InputCRUDModel

@Serializable
public data class InputUser(
    val username: String,
    val role: UserRole,
) : InputCRUDModel<User, UserId> {
    override fun toModel(id: UserId): User = User(
        id = id,
        username = username,
        role = role,
    )
}
