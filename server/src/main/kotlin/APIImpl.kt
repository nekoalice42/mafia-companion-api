package me.nekoalice.mafia.api.server

import io.ktor.http.*
import kotlinx.coroutines.flow.toList
import me.nekoalice.mafia.api.contracts.APIInfo
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.contracts.validation.validate
import me.nekoalice.mafia.api.dto.models.*
import me.nekoalice.mafia.api.server.storage.base.CRUDStorage
import me.nekoalice.mafia.api.server.storage.base.GameStorage
import me.nekoalice.mafia.api.server.storage.base.PlayerStorage
import me.nekoalice.mafia.api.server.storage.base.TournamentStorage
import me.nekoalice.mafia.api.server.utils.calculateScoreboard

class APIImpl(
    val tournamentStorage: TournamentStorage,
    val gameStorage: GameStorage,
    val playerStorage: PlayerStorage,
) : BaseAPI(
    info = APIInfo(
        name = "mafia-companion-api",
        version = "0.1.0-alpha.1",
        licenseIdentifier = "AGPL-3.0",
        developmentUrl = "http://localhost:8080",
        productionUrl = "https://api.mafia.nekoalice.me",
    )
) {
    private suspend fun <StorageT : CRUDStorage<ItemT, IdT>, ItemT, IdT> upsert(
        storage: StorageT,
        item: ItemT,
        id: IdT,
    ): Response<Unit> {
        val existed = storage.getByIdOrNull(id) != null
        storage.editOrAdd(id, item)
        return when (existed) {
            true -> Response.Success(HttpStatusCode.NoContent)
            false -> Response.Success(HttpStatusCode.Created)
        }
    }

    override suspend fun getRoot(): Response<HelloResponse> =
        Response.Success(HelloResponse())

    override suspend fun getPlayers(): Response<ResponseList<Player>> =
        Response.Success(ResponseList(playerStorage.getAll().toList()))

    override suspend fun upsertPlayer(player: Player): Response<Unit> =
        upsert(playerStorage, player, player.id)

    override suspend fun deletePlayer(playerId: PlayerId): Response<Unit> {
        playerStorage.delete(playerId)
        return Response.Success(HttpStatusCode.NoContent)
    }

    override suspend fun getTournaments(): Response<ResponseList<Tournament>> {
        return Response.Success(ResponseList(tournamentStorage.getAll().toList()))
    }

    override suspend fun getTournament(id: TournamentId): Response<Tournament> =
        tournamentStorage.getByIdOrNull(id)?.let {
            Response.Success(it)
        } ?: Response.Error("Tournament not found", HttpStatusCode.NotFound)

    override suspend fun upsertTournament(tournament: Tournament): Response<Unit> =
        upsert(tournamentStorage, tournament, tournament.id)

    override suspend fun deleteTournament(tournamentId: TournamentId): Response<Unit> {
        tournamentStorage.delete(tournamentId)
        return Response.Success(HttpStatusCode.NoContent)
    }

    override suspend fun createGame(game: NewGameBody): Response<Unit> {
        if (tournamentStorage.getByIdOrNull(game.tournament) == null) {
            return Response.Error(
                "Tournament ${game.tournament} not found",
                HttpStatusCode.NotFound,
            )
        }
        try {
            game.validate()
        } catch (e: IllegalArgumentException) {
            return Response.Error(e, HttpStatusCode.UnprocessableEntity)
        }
        gameStorage.create(game)
        return Response.Success(HttpStatusCode.Created)
    }

    override suspend fun getScoreboard(
        tournamentId: TournamentId,
    ): Response<ResponseList<ScoreboardRow>> {
        tournamentStorage.getByIdOrNull(tournamentId) ?: return Response.Error(
            "Tournament $tournamentId not found",
            HttpStatusCode.NotFound,
        )
        val scoreboardSorted = calculateScoreboard(gameStorage.getAll(tournamentId).toList())
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
        return Response.Success(ResponseList(scoreboardSorted))
    }
}
