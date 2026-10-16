package me.nekoalice.mafia.api.dao

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.uuid.Uuid

public object InGamePlayers : CompositeIdTable("in_game_players") {
    public val gameId: Column<EntityID<Uuid>> = reference("game_id", Games)
    public val playerId: Column<EntityID<Uuid>> = reference("player_id", Players)

    public val seat: Column<Int> = integer("seat").check { (it greaterEq 1) and (it lessEq 10) }
    public val role: Column<Role> = enumerationByName<Role>("role")
    public val extraPoints: Column<Int?> = integer("extra_points").nullable()
    public val extraPointsDescription: Column<String?> = text("extra_points_description").nullable()
    public val guessedMafiaCount: Column<Int?> = integer("guessed_mafia_count").nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(gameId, playerId)

    init {
        check {
            extraPoints.isNull() eq extraPointsDescription.isNull()
        }
    }
}
