package me.nekoalice.mafia.api.server.storage.base

import me.nekoalice.mafia.api.dto.tournament.Tournament
import me.nekoalice.mafia.api.dto.tournament.TournamentId

interface TournamentStorage : CRUDStorage<Tournament, TournamentId>
