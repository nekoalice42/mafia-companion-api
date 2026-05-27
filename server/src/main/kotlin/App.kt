package me.nekoalice.mafia.api.server

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import me.nekoalice.mafia.api.dto.models.*
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

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

private val jsonConfig = Json(DefaultJson) {
    prettyPrint = true
}

private val openapiJsonConfig = Json(jsonConfig) {
    encodeDefaults = false
}

fun Application.module() {
    val gameStorage = InMemoryGameStorage()
    val playerStorage = InMemoryPlayerStorage()
    install(ContentNegotiation) {
        json(jsonConfig)
    }
//    install(Resources)
    install(CORS) {
        anyHost()
        anyMethod()
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
    }
    routing {
        get("/") {
            call.respond(mapOf("message" to "Hello, World!"))
        }

        get("/scoreboard") {
            val scoreboardSorted = calculateScoreboard(gameStorage.getAll().toList())
                .map {
                    it.toScoreboardRow(
                        playerStorage.getByIdOrNull(it.playerId)
                            ?: Player(it.playerId, "Unknown player ${it.playerId}")
                    )
                }
                .sortedWith(
                    compareByDescending<ScoreboardRow> { it.totalPointsX100 }
                        .thenByDescending { it.playCount.total }
                        .thenByDescending { it.winCount.total }
                        .thenBy { it.player.nickname }
                )
            call.respond(ResponseList(scoreboardSorted))
        }

        get("/player") {
            call.respond(ResponseList(playerStorage.getAll().toList()))
        }

        /**
         * Create a new player
         *
         * Body: application/json [me.nekoalice.mafia.api.dto.models.Player]
         */
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

        /**
         * Create a new game
         *
         * Body: application/json [me.nekoalice.mafia.api.dto.models.NewGameBody]
         */
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

        @OptIn(ExperimentalKtorApi::class)
        get("/openapi.json") {
            val doc = OpenApiDoc(
                info = OpenApiInfo(
                    title = "mafia-companion-api",
                    version = "0.1.0-alpha.0",
                    license = OpenApiInfo.License(
                        name = "AGPLv3",
                        url = "https://www.gnu.org/licenses/agpl-3.0.html",
                        identifier = "AGPL-3.0",
                    ),
                ),
                servers = listOf(
                    Server(
                        url = "http://localhost:8080",
                        description = "Development server",
                    ),
                    Server(
                        url = "https://api.mafia.nekoalice.me",
                        description = "Production server",
                    )
                ),
            ) + call.application.routingRoot.descendants()
            val encodedDoc = openapiJsonConfig.encodeToString(doc)
            call.respondText(encodedDoc, ContentType.Application.Json)
        }.hide()

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
