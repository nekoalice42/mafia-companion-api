package me.nekoalice.mafia.api.dto.models

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
public data class NewGameBody(
    val players: List<InGamePlayer>,
    val winnerTeam: Team,
    val startTime: LocalDateTime,
) {
    public fun validate(): Result<NewGameBody> {
        try {
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
                if (player.extraPoints != null) {
                    require(player.extraPoints.pointsX100 % 10 == 0) {
                        "extra points must be a multiple of 0.1 (id=${player.playerId})"
                    }
                    require(player.extraPoints.pointsX100 in allowedExtraPointsX100) {
                        "extra points value must be -1.5, -1.0, -0.8..-0.2, 0.2..0.8, 1.0" +
                                " (id=${player.playerId})"
                    }
                    extraPointsCounter++
                    if (player.extraPoints.pointsX100 > 0) {
                        extraPointsX100PositiveSum += player.extraPoints.pointsX100
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
        } catch (e: IllegalArgumentException) {
            return Result.failure(e)
        }
        return Result.success(this)
    }
}
