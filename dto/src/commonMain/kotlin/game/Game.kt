package me.nekoalice.mafia.api.dto.game

import kotlinx.serialization.Serializable
import me.nekoalice.mafia.api.dto.base.CRUDModel
import me.nekoalice.mafia.api.dto.game.enums.Team
import kotlin.time.Instant

@Serializable
public data class Game(
    override val id: GameId,
    val players: List<InGamePlayer>,
    val winnerTeam: Team?,
    val startTime: Instant,
) : CRUDModel<GameId>
