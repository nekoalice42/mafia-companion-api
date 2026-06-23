package me.nekoalice.mafia.api.dao

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
public object Games : UuidTable("games", uuidVersion = UuidVersion.V7) {
    public val tournamentId: Column<EntityID<Uuid>> = reference("tournament_id", Tournaments)

    public val winnerTeam: Column<WinnerTeam> = enumerationByName<WinnerTeam>("winner_team")
    public val startedAt: Column<Instant> = timestampWithTimeZoneAsInstant("started_at")
}
