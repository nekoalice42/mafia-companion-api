package me.nekoalice.mafia.api.dto.tournament

import kotlinx.serialization.Serializable
import me.nekoalice.mafia.api.dto.base.InputCRUDModel
import kotlin.time.Instant

@Serializable
public data class InputTournament(
    val name: String,
    val startsAt: Instant,
    val endsAt: Instant?,
    val participatingPlayers: List<PlayerStartingPoints>,
    val scoreboardFreezesAt: Instant?,
) : InputCRUDModel<Tournament, TournamentId> {
    override fun toModel(id: TournamentId): Tournament = Tournament(
        id = id,
        name = name,
        startsAt = startsAt,
        endsAt = endsAt,
        participatingPlayers = participatingPlayers,
        scoreboardFreezesAt = scoreboardFreezesAt,
    )
}
