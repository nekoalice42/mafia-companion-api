package me.nekoalice.mafia.api.dto.tournament

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
public data class Tournament(
    val id: TournamentId,
    val name: String,
    val startDate: Instant,
    val endDate: Instant?,
)
