package me.nekoalice.mafia.api.dto.tournament

import kotlinx.serialization.Serializable
import me.nekoalice.mafia.api.dto.base.InputCRUDModel
import kotlin.time.Instant

@Serializable
public data class InputTournament(
    val name: String,
    val startDate: Instant,
    val endDate: Instant?,
) : InputCRUDModel<Tournament, TournamentId> {
    override fun toModel(id: TournamentId): Tournament = Tournament(
        id = id,
        name = name,
        startDate = startDate,
        endDate = endDate,
    )
}
