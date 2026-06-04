package me.nekoalice.mafia.api.server.storage.base

import me.nekoalice.mafia.api.dto.models.Tournament
import me.nekoalice.mafia.api.dto.models.TournamentId

interface TournamentStorage : CRUDStorage<Tournament, TournamentId>
