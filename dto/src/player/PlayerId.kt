package me.nekoalice.mafia.api.dto.player

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
@OptIn(ExperimentalUuidApi::class)
@Serializable
public value class PlayerId(public val value: Uuid) {
    public constructor(value: String) : this(Uuid.parse(value))

    override fun toString(): String = value.toString()

    public companion object {
        public fun new(): PlayerId = PlayerId(Uuid.generateV7())
    }
}
