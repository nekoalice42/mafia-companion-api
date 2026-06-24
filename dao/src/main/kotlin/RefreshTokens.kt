package me.nekoalice.mafia.api.dao

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import kotlin.time.Instant
import kotlin.uuid.Uuid

public object RefreshTokens : IdTable<Uuid>("refresh_tokens") {
    public override val id: Column<EntityID<Uuid>> = reference("user_id", Users)

    public val hash: Column<String> = text("hash")
    public val expiresAt: Column<Instant> = timestampWithTimeZoneAsInstant("expires_at")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
