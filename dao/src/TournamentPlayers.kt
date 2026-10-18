package me.nekoalice.mafia.api.dao

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.greaterEq
import kotlin.uuid.Uuid

public object TournamentPlayers : CompositeIdTable("tournament_players") {
    public val tournamentId: Column<EntityID<Uuid>> = reference("tournament_id", Tournaments)
    public val playerId: Column<EntityID<Uuid>> = reference("player_id", Players)

    public val startingPoints: Column<Int> = integer("starting_points")

    override val primaryKey: PrimaryKey = PrimaryKey(tournamentId, playerId)

    init {
        check { startingPoints greaterEq 0 }
    }
}
