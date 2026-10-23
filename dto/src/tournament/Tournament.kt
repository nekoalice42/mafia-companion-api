package me.nekoalice.mafia.api.dto.tournament

import kotlinx.serialization.Serializable
import me.nekoalice.mafia.api.dto.base.CRUDModel
import kotlin.time.Instant

@Serializable
public data class Tournament(
    override val id: TournamentId,
    val name: String,
    val startsAt: Instant,
    val endsAt: Instant?,
    val participatingPlayers: List<PlayerStartingPoints>,
    val scoreboardFreezesAt: Instant?,
) : CRUDModel<TournamentId>
