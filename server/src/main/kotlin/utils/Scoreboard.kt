package me.nekoalice.mafia.api.server.utils

import me.nekoalice.mafia.api.dto.game.NewGameBody
import me.nekoalice.mafia.api.dto.player.PlayerId
import me.nekoalice.mafia.api.dto.tournament.scoreboard.ScoreboardRow

fun calculateScoreboard(games: Iterable<NewGameBody>): List<ScoreboardRowRollingCounter> {
    val playerCounters = mutableMapOf<PlayerId, ScoreboardRowRollingCounter>()
    for (game in games) {
        for (player in game.players) {
            playerCounters
                .getOrPut(player.playerId) { ScoreboardRowRollingCounter(player.playerId) }
                .countGame(
                    playerRole = player.role,
                    winnerTeam = game.winnerTeam,
                    extraPointsX100 = player.extraPoints?.pointsX100 ?: 0,
                    wasKilledFirstNight = player.guessedMafiaCount != null,
                    guessedMafiaCount = player.guessedMafiaCount ?: 0,
                )
        }
    }
    playerCounters.values.forEach { it.calculateCiPoints() }
    return playerCounters.values.toList()
}

fun setPlaces(games: Iterable<ScoreboardRow>): List<ScoreboardRow> = buildList {
    var previousValue: ScoreboardRow? = null
    for (current in games) {
        val newPlace = if (previousValue?.totalPointsX100 != current.totalPointsX100)
            (previousValue?.place ?: 0u) + 1u
        else
            previousValue.place
        add(current.copy(place = newPlace))
        previousValue = current
    }
}
