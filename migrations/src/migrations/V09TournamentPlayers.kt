package me.nekoalice.mafia.api.migrations.migrations

import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils

object V09TournamentPlayers : Migration {
    override val version: UInt = 9u

    context(transaction: R2dbcTransaction)
    override suspend fun up() {
        SchemaUtils.create(Tables.TournamentPlayers)
    }

    context(transaction: R2dbcTransaction)
    override suspend fun down() {
        SchemaUtils.drop(Tables.TournamentPlayers)
    }

    private object Tables {
        private object Tournaments : UuidTable("tournaments", uuidVersion = UuidVersion.V7)
        private object Players : UuidTable("players", uuidVersion = UuidVersion.V7)

        object TournamentPlayers : CompositeIdTable("tournament_players") {
            val tournamentId = reference("tournament_id", Tournaments)
            val playerId = reference("player_id", Players)

            val startingPoints = integer("starting_points")

            override val primaryKey = PrimaryKey(tournamentId, playerId)

            init {
                check { startingPoints greaterEq 0 }
            }
        }
    }
}
