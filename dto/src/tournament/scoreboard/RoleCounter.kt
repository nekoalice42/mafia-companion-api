package me.nekoalice.mafia.api.dto.tournament.scoreboard

import kotlinx.serialization.Serializable
import me.nekoalice.mafia.api.dto.game.enums.Role

@Serializable
public data class RoleCounter(
    var mafia: Int = 0,
    var don: Int = 0,
    var sheriff: Int = 0,
    var citizen: Int = 0,
) {
    public operator fun get(role: Role): Int = when (role) {
        Mafia -> mafia
        Don -> don
        Sheriff -> sheriff
        Citizen -> citizen
    }

    public operator fun set(role: Role, value: Int) {
        when (role) {
            Mafia -> mafia = value
            Don -> don = value
            Sheriff -> sheriff = value
            Citizen -> citizen = value
        }
    }

    public val total: Int
        get() = mafia + don + sheriff + citizen
}
