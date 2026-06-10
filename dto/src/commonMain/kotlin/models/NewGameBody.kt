package me.nekoalice.mafia.api.dto.models

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
public data class NewGameBody(
    val tournament: TournamentId,
    val players: List<InGamePlayer>,
    val winnerTeam: Team,
    val startTime: Instant,
)
