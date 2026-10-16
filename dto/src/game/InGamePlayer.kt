package me.nekoalice.mafia.api.dto.game

import kotlinx.serialization.Serializable
import me.nekoalice.mafia.api.dto.player.PlayerId
import me.nekoalice.mafia.api.dto.game.enums.Role

@Serializable
public data class InGamePlayer(
    val playerId: PlayerId,
    val role: Role,
    val extraPoints: ExtraPointsDescribed?,
    val guessedMafiaCount: Int?,
)
