package me.nekoalice.mafia.api.server.validation

import me.nekoalice.mafia.api.dto.game.NewGameBody
import me.nekoalice.mafia.api.dto.player.PlayerId
import me.nekoalice.mafia.api.dto.game.enums.Role
import kotlin.time.Clock

fun NewGameBody.validate() {
    require(players.size == 10) { "players != 10" }
    require(
        startTime < Clock.System.now()
    ) { "game started in the future" }

    val playerIdsUnique = mutableSetOf<PlayerId>()
    val playerRoles = mutableMapOf(
        Role.Mafia to 0,
        Role.Don to 0,
        Role.Sheriff to 0,
        Role.Citizen to 0,
    )
    val playerRolesRequired = mapOf(
        Role.Mafia to 2,
        Role.Don to 1,
        Role.Sheriff to 1,
        Role.Citizen to 6,
    )
    // -2.5, -2.1, -1.5, -1.0..-0.2, 0.2..1.0
    val allowedExtraPointsX100 = sequence {
        (2..10).forEach {
            yield(it * 10)
            yield(-it * 10)
        }
        yield(-150)
        yield(-210)
        yield(-250)
    }.toSet()
    var extraPointsX100PositiveSum = 0
    var extraPointsCounter = 0
    var hasFirstNightDeath = false

    for (player in players) {
        require(playerIdsUnique.add(player.playerId)) {
            "duplicate player detected (id=${player.playerId})"
        }
        playerRoles[player.role] = playerRoles[player.role]!! + 1
        require(playerRoles[player.role]!! <= playerRolesRequired[player.role]!!) {
            "too many ${player.role} players"
        }
        val playerExtraPoints = player.extraPoints
        if (playerExtraPoints != null) {
            if (winnerTeam == null) {
                require(playerExtraPoints.pointsX100 <= 0) {
                    "Extra points cannot be positive if there is no winner (id=${player.playerId})"
                }
            }
            require(playerExtraPoints.pointsX100 % 10 == 0) {
                "extra points must be a multiple of 0.1 (id=${player.playerId})"
            }
            require(playerExtraPoints.pointsX100 in allowedExtraPointsX100) {
                val ptsStr = allowedExtraPointsX100.joinToString()
                "extra points value must be one of $ptsStr (id=${player.playerId})"
            }
            extraPointsCounter++
            if (playerExtraPoints.pointsX100 > 0) {
                extraPointsX100PositiveSum += playerExtraPoints.pointsX100
            }
            require(extraPointsCounter <= 7) { "too many players with extra points" }
            require(extraPointsX100PositiveSum <= 400) {
                "total positive extra points cannot exceed 4.0"
            }
        }
        if (player.guessedMafiaCount != null) {
            require(!hasFirstNightDeath) {
                "not more than one player can be killed first night (id=${player.playerId})"
            }
            require(player.guessedMafiaCount in 0..3) {
                "guessed mafia count cannot be negative or exceed 3 (id=${player.playerId})"
            }
            hasFirstNightDeath = true
        }
    }
}
