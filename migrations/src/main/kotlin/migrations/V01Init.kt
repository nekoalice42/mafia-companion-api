package me.nekoalice.mafia.api.migrations.migrations

import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import kotlin.uuid.ExperimentalUuidApi

@Suppress("unused")
@OptIn(ExperimentalUuidApi::class)
private object Tables {
    object Games : UuidTable("games", uuidVersion = UuidVersion.V7) {
        val tournamentId = uuid("tournament_id").references(Tournaments.id)
        val winnerTeam = text("winner_team")
        val startedAt = timestamp("started_at")
    }

    object InGamePlayers : CompositeIdTable("in_game_players") {
        val gameId = uuid("game_id").references(Games.id).entityId()
        val playerId = uuid("player_id").references(Players.id).entityId()
        val role = text("role")
        val extraPoints = integer("extra_points").nullable()
        val extraPointsDescription = text("extra_points_description").nullable()

        val guessedMafiaCount = integer("guessed_mafia_count").nullable()

        override val primaryKey = PrimaryKey(gameId, playerId)

        init {
            check {
                extraPoints.isNull() eq extraPointsDescription.isNull()
            }
        }
    }

    object Players : UuidTable("players", uuidVersion = UuidVersion.V7) {
        val nickname = text("nickname").uniqueIndex()
    }

    object Tournaments : UuidTable("tournaments", uuidVersion = UuidVersion.V7) {
        val name = text("name")
        val startsAt = timestamp("starts_at")
        val endsAt = timestamp("ends_at").nullable()
    }

    val all = arrayOf(Games, InGamePlayers, Players, Tournaments)
}

object V01Init : Migration {
    override val version = 1

    context(transaction: R2dbcTransaction)
    override suspend fun up() {
        SchemaUtils.create(*Tables.all)
    }

    context(transaction: R2dbcTransaction)
    override suspend fun down() {
        SchemaUtils.drop(*Tables.all)
    }
}
