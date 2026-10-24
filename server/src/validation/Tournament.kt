package me.nekoalice.mafia.api.server.validation

import me.nekoalice.mafia.api.dto.tournament.InputTournament
import me.nekoalice.mafia.api.dto.tournament.Tournament
import me.nekoalice.mafia.api.server.utils.participatingPlayerIds

fun InputTournament.validateNew() = runValidation {
    if (scoreboardFreezesAt != null && endsAt != null) {
        require(scoreboardFreezesAt!! <= endsAt!!) {
            "'scoreboardFreezesAt' cannot be after 'endsAt'"
        }
    }
}

fun InputTournament.validateEdited(old: Tournament, hasGames: Boolean) = runValidation {
    // It's fine re-throwing the exception here, we're already in a validation context
    validateNew().getOrThrow()
    if (hasGames && old.participatingPlayerIds != participatingPlayerIds) {
        throw IllegalArgumentException(
            "Cannot change participating players when the tournament already has games"
        )
    }
}
