package me.nekoalice.mafia.api.dao

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import kotlin.time.Instant

public object ClientStates : Table("client_states") {
    public val state: Column<String> = text("state")

    public val redirectUrl: Column<String?> = text("redirect_url").nullable()
    public val clientState: Column<String?> = text("client_state").nullable()
    public val expiresAt: Column<Instant> = timestampWithTimeZoneAsInstant("expires_at")

    override val primaryKey: PrimaryKey = PrimaryKey(state)
}
