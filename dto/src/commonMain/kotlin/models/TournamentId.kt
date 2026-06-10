package me.nekoalice.mafia.api.dto.models

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
@Serializable
@OptIn(ExperimentalUuidApi::class)
public value class TournamentId(public val value: Uuid) {
    public constructor(value: String) : this(Uuid.parse(value))

    override fun toString(): String = value.toString()

    public companion object {
        public fun new(): TournamentId = TournamentId(Uuid.generateV7())
    }
}
