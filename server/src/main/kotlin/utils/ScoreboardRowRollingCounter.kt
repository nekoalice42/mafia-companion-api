package me.nekoalice.mafia.api.server.utils

import me.nekoalice.mafia.api.dto.models.Player
import me.nekoalice.mafia.api.dto.models.PlayerId
import me.nekoalice.mafia.api.dto.models.Role
import me.nekoalice.mafia.api.dto.models.RoleCounter
import me.nekoalice.mafia.api.dto.models.ScoreboardRow
import me.nekoalice.mafia.api.dto.models.Team

class ScoreboardRowRollingCounter(
    val playerId: PlayerId,
) {
    private val playCount = RoleCounter()
    private val winCount = RoleCounter()
    private var gamePointsX100: Int = 0
    private var bestTurnPointsX100: Int = 0
    private var ciPointsX100: Int = 0
    private var firstNightDeaths: Int = 0
    private var totalPlayCount = 0
    private val ciDividers = mutableListOf<Int?>() // Int? is filtered easier than Int, see below

    fun countGame(
        playerRole: Role,
        winnerTeam: Team,
        extraPointsX100: Int = 0,
        wasKilledFirstNight: Boolean,
        guessedMafiaCount: Int,
    ) {
        require(!wasKilledFirstNight && guessedMafiaCount == 0 || wasKilledFirstNight) {
            "Guessed mafia count cannot be non-zero if not killed first night"
        }
        gamePointsX100 += extraPointsX100
        playCount[playerRole] = playCount[playerRole] + 1
        totalPlayCount++
        if (winnerTeam == playerRole.team) {
            winCount[playerRole] = winCount[playerRole] + 1
            gamePointsX100 += 100
        }
        if (wasKilledFirstNight) {
            firstNightDeaths++
            if (playerRole.team == Team.Citizen) {
                bestTurnPointsX100 += when (guessedMafiaCount) {
                    0 -> 0
                    1 -> 10
                    2 -> 30
                    3 -> 60
                    else -> throw IllegalStateException()
                }
                val divider = when {
                    // This is still counted as first night death, but no score should be added.
                    // I'm not using `firstNightDeaths` instead because `Team.Mafia` player
                    //  may be killed first night, and maintaining a separate counter for this
                    //  is pointless IMO.
                    guessedMafiaCount == 0 -> null
                    winnerTeam == Team.Citizen -> 1
                    else -> 2
                }
                ciDividers.add(divider)
            }
        }
    }

    fun calculateCiPoints() {
        if (totalPlayCount < 4) return
        val b = (totalPlayCount * 0.4).roundedToNearestInt
        val baseCiX100 = (ciDividers.size * 40 roundDiv b).coerceAtMost(40)
        for (k in ciDividers.filterNotNull()) {
            ciPointsX100 += (baseCiX100 roundDiv k)
        }
    }

    fun toScoreboardRow(player: Player): ScoreboardRow {
        require(player.id == playerId) { "Player ID mismatch: ${player.id} != $playerId" }
        return ScoreboardRow(
            player,
            playCount,
            winCount,
            gamePointsX100,
            ciPointsX100,
            bestTurnPointsX100,
        )
    }
}
