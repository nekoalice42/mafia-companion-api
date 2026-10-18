package me.nekoalice.mafia.api.server.utils

import me.nekoalice.mafia.api.dto.game.Game
import me.nekoalice.mafia.api.dto.player.PlayerId
import me.nekoalice.mafia.api.dto.tournament.PlayerStartingPoints
import me.nekoalice.mafia.api.dto.tournament.scoreboard.ScoreboardRow

fun calculateScoreboard(
    games: Iterable<Game>,
    startingPoints: List<PlayerStartingPoints>?,
): List<ScoreboardRowRollingCounter> {
    val playerCounters = mutableMapOf<PlayerId, ScoreboardRowRollingCounter>()
    val playerToStartingPoints = startingPoints?.associate {
        it.playerId to it
    } ?: emptyMap()
    playerCounters.putAll(
        playerToStartingPoints.mapValues {
            ScoreboardRowRollingCounter(
                playerId = it.key,
                startingPointsX100 = it.value.startingPointsX100,
            )
        }
    )
    for (game in games) {
        for (player in game.players) {
            playerCounters
                .getOrPut(player.playerId) {
                    ScoreboardRowRollingCounter(
                        player.playerId,
                        playerToStartingPoints[player.playerId]?.startingPointsX100 ?: 0,
                    )
                }
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
        val newPlace = when {
            previousValue == null -> 1u
            previousValue.totalPointsX100 != current.totalPointsX100 -> (previousValue.place ?: 0u) + 1u
            else -> previousValue.place
        }
        val currentWithPlace = current.copy(place = newPlace)
        add(currentWithPlace)
        previousValue = currentWithPlace
    }
}
