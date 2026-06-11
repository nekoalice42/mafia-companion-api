package me.nekoalice.mafia.api.dao

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Instant

public object Tournaments : UuidTable("tournaments", uuidVersion = UuidVersion.V7) {
    public val name: Column<String> = text("name", eagerLoading = true)
    public val startsAt: Column<Instant> = timestamp("starts_at")
    public val endsAt: Column<Instant?> = timestamp("ends_at").nullable()
}
