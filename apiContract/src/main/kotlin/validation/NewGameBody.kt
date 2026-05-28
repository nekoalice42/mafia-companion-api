package me.nekoalice.mafia.api.contracts.validation

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.nekoalice.mafia.api.dto.models.NewGameBody
import me.nekoalice.mafia.api.dto.models.PlayerId
import me.nekoalice.mafia.api.dto.models.Role
import kotlin.time.Clock

public fun NewGameBody.validate() {
    require(players.size == 10) { "players != 10" }
    require(
        startTime < Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
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
    // -1.5, -1.0, -0.8..-0.2, 0.2..0.8, 1.0
    val allowedExtraPointsX100 = sequence {
        yield(-150)
        yield(-100)
        (2..8).forEach {
            yield(it * 10)
            yield(-it * 10)
        }
        yield(100)
    }.toList()
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
            require(playerExtraPoints.pointsX100 % 10 == 0) {
                "extra points must be a multiple of 0.1 (id=${player.playerId})"
            }
            require(playerExtraPoints.pointsX100 in allowedExtraPointsX100) {
                "extra points value must be -1.5, -1.0, -0.8..-0.2, 0.2..0.8, 1.0" +
                        " (id=${player.playerId})"
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
