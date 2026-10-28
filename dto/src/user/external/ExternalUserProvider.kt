package me.nekoalice.mafia.api.dto.user.external

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class ExternalUserProvider {
    @SerialName("telegram")
    Telegram,
    ;
}
