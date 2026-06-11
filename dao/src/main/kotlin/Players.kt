package me.nekoalice.mafia.api.dao

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.UuidTable

public object Players : UuidTable("players", uuidVersion = UuidVersion.V7) {
    public val nickname: Column<String> = text("nickname", eagerLoading = true).uniqueIndex()
}
