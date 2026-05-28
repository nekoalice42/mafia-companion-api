package me.nekoalice.mafia.api.dto.models

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
public data class NewGameBody(
    val players: List<InGamePlayer>,
    val winnerTeam: Team,
    val startTime: LocalDateTime,
)
