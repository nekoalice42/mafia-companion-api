package me.nekoalice.mafia.api.dao

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
public object Games : UuidTable("games", uuidVersion = UuidVersion.V7) {
    public val tournamentId: Column<Uuid> = uuid("tournament_id").references(Tournaments.id)
    public val winnerTeam: Column<WinnerTeam> = enumerationByName<WinnerTeam>("winner_team")
    public val startedAt: Column<Instant> = timestamp("started_at")
}
