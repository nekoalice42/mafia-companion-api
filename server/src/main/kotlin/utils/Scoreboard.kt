package me.nekoalice.mafia.api.server.utils

import me.nekoalice.mafia.api.dto.models.NewGameBody
import me.nekoalice.mafia.api.dto.models.PlayerId

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
