package me.nekoalice.mafia.api.dao

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

public object Users : UuidTable("users", uuidVersion = UuidVersion.V7) {
    public val username: Column<String> = text("username", eagerLoading = true).uniqueIndex()
    public val passwordHash: Column<String?> = text("password_hash", eagerLoading = true).nullable()
}
