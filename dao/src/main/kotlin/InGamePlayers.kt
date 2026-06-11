package me.nekoalice.mafia.api.dao

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
public object InGamePlayers : CompositeIdTable("in_game_players") {
    public val gameId: Column<EntityID<Uuid>> = uuid("game_id").references(Games.id).entityId()
    public val playerId: Column<EntityID<Uuid>> =
        uuid("player_id").references(Players.id).entityId()
    public val role: Column<Role> = enumerationByName<Role>("role")
    public val extraPoints: Column<Int?> = integer("extra_points").nullable()
    public val extraPointsDescription: Column<String?> =
        text("extra_points_description", eagerLoading = true).nullable()
    public val guessedMafiaCount: Column<Int?> = integer("guessed_mafia_count").nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(gameId, playerId)

    init {
        check {
            extraPoints.isNull() eq extraPointsDescription.isNull()
        }
    }
}
