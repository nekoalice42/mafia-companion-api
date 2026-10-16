package me.nekoalice.mafia.api.server.storage.inmemory

import me.nekoalice.mafia.api.dto.tournament.InputTournament
import me.nekoalice.mafia.api.dto.tournament.Tournament
import me.nekoalice.mafia.api.dto.tournament.TournamentId
import me.nekoalice.mafia.api.server.storage.base.TournamentStorage

class InMemoryTournamentStorage : TournamentStorage {
    private val tournaments = mutableMapOf<TournamentId, Tournament>()

    override suspend fun add(item: InputTournament): TournamentId =
        TournamentId.new().also { id -> tournaments[id] = item.toModel(id) }

    override suspend fun edit(id: TournamentId, item: InputTournament) {
        if (tournaments.containsKey(id)) tournaments[id] = item.toModel(id)
    }

    override suspend fun getByIdOrNull(id: TournamentId): Tournament? = tournaments[id]

    override suspend fun getAll(): List<Tournament> = tournaments.values.toList()

    override suspend fun delete(id: TournamentId) {
        tournaments.remove(id)
    }
}
