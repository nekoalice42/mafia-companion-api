package me.nekoalice.mafia.api.dto.response

import kotlinx.serialization.Serializable
import me.nekoalice.mafia.api.dto.game.GameId
import me.nekoalice.mafia.api.dto.player.PlayerId
import me.nekoalice.mafia.api.dto.tournament.TournamentId
import me.nekoalice.mafia.api.dto.user.UserId
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
public data class NewResourceId(val id: Uuid) {
    public constructor(rawValue: String) : this(Uuid.parse(rawValue))

    override fun toString(): String = id.toString()

    public companion object {
        @OptIn(ExperimentalUuidApi::class)
        public fun new(): NewResourceId = NewResourceId(Uuid.generateV7())
    }
}

public fun NewResourceId(gameId: GameId): NewResourceId = NewResourceId(gameId.value)
public fun NewResourceId(playerId: PlayerId): NewResourceId = NewResourceId(playerId.value)
public fun NewResourceId(tournamentId: TournamentId): NewResourceId =
    NewResourceId(tournamentId.value)

public fun NewResourceId(userId: UserId): NewResourceId = NewResourceId(userId.value)
