package me.nekoalice.mafia.api.dto.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class Role(public val team: Team) {
    @SerialName("mafia")
    Mafia(Team.Mafia),

    @SerialName("don")
    Don(Team.Mafia),

    @SerialName("sheriff")
    Sheriff(Team.Citizen),

    @SerialName("citizen")
    Citizen(Team.Citizen),
    ;
}
