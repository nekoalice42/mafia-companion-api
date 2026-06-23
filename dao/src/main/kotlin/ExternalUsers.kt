package me.nekoalice.mafia.api.dao

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.uuid.Uuid

public object ExternalUsers : CompositeIdTable("external_users") {
    public val externalId: Column<EntityID<String>> =
        text("external_id", eagerLoading = true).entityId()
    public val provider: Column<EntityID<ExternalProvider>> =
        enumerationByName<ExternalProvider>("provider").entityId()

    public val userId: Column<EntityID<Uuid>> = reference("user_id", Users)

    override val primaryKey: PrimaryKey = PrimaryKey(externalId, provider)
}
