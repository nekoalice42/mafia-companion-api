package me.nekoalice.mafia.api.server.storage.inmemory

import me.nekoalice.mafia.api.dto.tournament.TournamentId
import me.nekoalice.mafia.api.server.storage.base.TournamentStorage

class InMemoryTournamentStorage : TournamentStorage, TournamentMemoryStorage(TournamentId::new)
