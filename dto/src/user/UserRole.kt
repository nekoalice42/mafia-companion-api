package me.nekoalice.mafia.api.dto.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class UserRole {
    @SerialName("admin")
    Admin,

    @SerialName("judge")
    Judge,
    ;
}
