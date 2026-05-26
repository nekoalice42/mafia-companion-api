package me.nekoalice.mafia.api

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.nekoalice.mafia.api.models.ErrorResponse
import me.nekoalice.mafia.api.models.ResponseList
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

interface Validatable<T : Validatable<T>> {
    fun validate(): Result<T>
}

@JvmInline
@Serializable
value class PlayerId(val value: String)

@Serializable
data class Player(
    val id: PlayerId,
    val nickname: String,
)

@Serializable
enum class Team {
    @SerialName("mafia")
    Mafia,

    @SerialName("citizen")
    Citizen,
    ;
}

@Serializable
enum class Role(val team: Team) {
    @SerialName("mafia")
    Mafia(Team.Mafia),

    @SerialName("don")
    Don(Team.Mafia),

    @SerialName("sheriff")
    Sheriff(Team.Citizen),

    @SerialName("citizen")
    Citizen(Team.Citizen),
    ;
}

@Serializable
data class ExtraPointsDescribed(
    val pointsX100: Int,
    val description: String,
)

@Serializable
data class InGamePlayer(
    val playerId: PlayerId,
    val role: Role,
    val extraPoints: ExtraPointsDescribed?,
    val guessedMafiaCount: Int?,
)

@Serializable
data class NewGameBody(
    val players: List<InGamePlayer>,
    val winnerTeam: Team,
    val startTime: LocalDateTime,
): Validatable<NewGameBody> {
    override fun validate(): Result<NewGameBody> {
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

@Serializable
data class ScoreboardRow(
    val player: Player,
    val playCount: Map<Role, Int>,
    val winCount: Map<Role, Int>,
    val gamePointsX100: Int,
    val ciPointsX100: Int,
    val bestTurnPointsX100: Int,
) {
    val totalPointsX100: Int
        get() = gamePointsX100 + ciPointsX100 + bestTurnPointsX100
}

private fun emptyPlayCount() = Role.entries.associateWith { 0 }.toMutableMap()

private val Double.roundedToNearestInt: Int
    get() = when {
        !isFinite() -> throw IllegalArgumentException("Invalid input: $this")
        this < 0 -> throw UnsupportedOperationException("Negative values are unsupported")
        else -> (this + 0.5).toInt()
    }

private infix fun Int.roundDiv(other: Int) = when {
    other <= 0 || this < 0 -> throw IllegalArgumentException("Invalid input: $this roundDiv $other")
    other == 1 -> this
    else -> (this + other / 2) / other
}

class ScoreboardRowRollingCounter(
    val playerId: PlayerId,
) {
    private val playCount: MutableMap<Role, Int> = emptyPlayCount()
    private val winCount: MutableMap<Role, Int> = emptyPlayCount()
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
        playCount[playerRole] = playCount[playerRole]!! + 1
        totalPlayCount++
        if (winnerTeam == playerRole.team) {
            winCount[playerRole] = winCount[playerRole]!! + 1
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

interface GameStorage {
    suspend fun create(game: NewGameBody)
    fun getAll(): Flow<NewGameBody>
}

interface PlayerStorage {
    suspend fun add(player: Player)
    suspend fun getByIdOrNull(id: PlayerId): Player?
    suspend fun edit(id: PlayerId, player: Player)
    fun getAll(): Flow<Player>
}

class InMemoryGameStorage : GameStorage {
    private val games = mutableListOf<NewGameBody>()

    override suspend fun create(game: NewGameBody) {
        games.add(game)
    }

    override fun getAll(): Flow<NewGameBody> = games.asFlow()
}

class InMemoryPlayerStorage : PlayerStorage {
    private val players = mutableMapOf<PlayerId, Player>()

    override suspend fun add(player: Player) {
        players[player.id] = player
    }

    override suspend fun edit(id: PlayerId, player: Player) {
        players[id] = player
    }

    override suspend fun getByIdOrNull(id: PlayerId): Player? = players[id]

    override fun getAll(): Flow<Player> = players.values.asFlow()
}

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

fun Application.module() {
    val gameStorage = InMemoryGameStorage()
    val playerStorage = InMemoryPlayerStorage()
    install(ContentNegotiation) {
        val jsonConfig = Json(DefaultJson) {
            prettyPrint = true
        }
        json(jsonConfig)
    }
//    install(Resources)
    routing {
        get("/") {
            call.respond(mapOf("message" to "Hello, World!"))
        }

        get("/scoreboard") {
            val scoreboardSorted = calculateScoreboard(gameStorage.getAll().toList())
                .map {
                    it.toScoreboardRow(playerStorage.getByIdOrNull(it.playerId)
                        ?: Player(it.playerId, "Unknown player ${it.playerId}"))
                }
                .sortedWith(
                    compareByDescending<ScoreboardRow> { it.totalPointsX100 }
                        .thenByDescending { it.playCount.values.sum() }
                        .thenByDescending { it.winCount.values.sum() }
                        .thenBy { it.player.nickname }
                )
            call.respond(ResponseList(scoreboardSorted))
        }

        get("/player") {
            call.respond(ResponseList(playerStorage.getAll().toList()))
        }

        post<Player>("/player") { newPlayer ->
            val existingPlayer = playerStorage.getByIdOrNull(newPlayer.id)
            if (existingPlayer != null) {
                playerStorage.edit(newPlayer.id, newPlayer)
                call.respond(HttpStatusCode.NoContent)
                return@post
            }
            playerStorage.add(newPlayer)
            call.respond(HttpStatusCode.Created)
        }

        post<NewGameBody>("/game") { newGame ->
            val validationResult = newGame.validate()
            if (validationResult.isFailure) {
                call.respond(
                    HttpStatusCode.UnprocessableEntity,
                    ErrorResponse(
                        validationResult.exceptionOrNull()?.message
                            ?: "Unknown error validating game data"
                    )
                )
                return@post
            }
            gameStorage.create(newGame)
            call.respond(HttpStatusCode.Created)
        }

        get("/apiSamples/newGameBody/randomUnvalidated") {
            call.respond(
                NewGameBody(
                    players = List(10) { i ->
                        InGamePlayer(
                            playerId = PlayerId("123$i"),
                            role = Role.entries.random(),
                            extraPoints = if (listOf(true, false).random())
                                ExtraPointsDescribed(
                                    pointsX100 = Random.nextInt(-6, 6) * 10,
                                    description = "Yes",
                                )
                            else null,
                            guessedMafiaCount = listOf(null, 0, 1, 2, 3).random(),
                        )
                    },
                    winnerTeam = Team.entries.random(),
                    startTime = (Clock.System.now() - 1.hours)
                        .toLocalDateTime(TimeZone.currentSystemDefault()),
                )
            )
        }
    }
}
