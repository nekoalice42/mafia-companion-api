package me.nekoalice.mafia.api.dao

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import kotlin.time.Instant

public object Tournaments : UuidTable("tournaments", uuidVersion = UuidVersion.V7) {
    public val name: Column<String> = text("name")
    public val startsAt: Column<Instant> = timestampWithTimeZoneAsInstant("starts_at")
    public val endsAt: Column<Instant?> = timestampWithTimeZoneAsInstant("ends_at").nullable()
    public val scoreboardFreezesAt: Column<Instant?> =
        timestampWithTimeZoneAsInstant("scoreboard_freezes_at").nullable()

    init {
        check {
            scoreboardFreezesAt.isNull() or endsAt.isNull() or (scoreboardFreezesAt lessEq endsAt)
        }
    }
}
