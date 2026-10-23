package me.nekoalice.mafia.api.server.validation

import me.nekoalice.mafia.api.dto.tournament.InputTournament
import me.nekoalice.mafia.api.dto.tournament.Tournament
import me.nekoalice.mafia.api.server.utils.participatingPlayerIds

fun InputTournament.validateNew() {
    if (scoreboardFreezesAt != null && endsAt != null) {
        require(scoreboardFreezesAt!! <= endsAt!!) {
            "'scoreboardFreezesAt' cannot be after 'endsAt'"
        }
    }
}

fun InputTournament.validateEdited(old: Tournament, hasGames: Boolean) {
    validateNew()
    if (hasGames && old.participatingPlayerIds != participatingPlayerIds) {
        throw IllegalArgumentException(
            "Cannot change participating players when the tournament already has games"
        )
    }
}
