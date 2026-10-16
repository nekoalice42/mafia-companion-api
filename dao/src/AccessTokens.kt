package me.nekoalice.mafia.api.dao

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.time.Instant
import kotlin.uuid.Uuid

public object AccessTokens : Table("access_tokens") {
    public val hash: Column<String> = text("hash")

    public val userId: Column<EntityID<Uuid>> = reference("user_id", Users)
    public val expiresAt: Column<Instant> = timestampWithTimeZoneAsInstant("expires_at")

    override val primaryKey: PrimaryKey = PrimaryKey(hash)
}
