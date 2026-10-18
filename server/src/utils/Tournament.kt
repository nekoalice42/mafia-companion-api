package me.nekoalice.mafia.api.server.utils

import me.nekoalice.mafia.api.dto.tournament.InputTournament
import me.nekoalice.mafia.api.dto.tournament.Tournament

val Tournament.participatingPlayerIds
    get() = participatingPlayers.map { it.playerId }.toSet()

val InputTournament.participatingPlayerIds
    get() = participatingPlayers.map { it.playerId }.toSet()
