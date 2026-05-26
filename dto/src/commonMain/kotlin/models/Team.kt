package me.nekoalice.mafia.api.dto.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class Team {
    @SerialName("mafia")
    Mafia,

    @SerialName("citizen")
    Citizen,
    ;
}
