package me.nekoalice.mafia.api.dto.tournament

import kotlinx.serialization.Serializable
import me.nekoalice.mafia.api.dto.base.CRUDModel
import kotlin.time.Instant

@Serializable
public data class Tournament(
    override val id: TournamentId,
    val name: String,
    val startDate: Instant,
    val endDate: Instant?,
    val participatingPlayers: List<PlayerStartingPoints>,
) : CRUDModel<TournamentId>
