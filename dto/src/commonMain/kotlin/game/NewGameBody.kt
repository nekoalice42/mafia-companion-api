package me.nekoalice.mafia.api.dto.game

import kotlinx.serialization.Serializable
import me.nekoalice.mafia.api.dto.game.enums.Team
import me.nekoalice.mafia.api.dto.tournament.TournamentId
import kotlin.time.Instant

@Serializable
public data class NewGameBody(
    val tournament: TournamentId,
    val players: List<InGamePlayer>,
    val winnerTeam: Team?,
    val startTime: Instant,
)
