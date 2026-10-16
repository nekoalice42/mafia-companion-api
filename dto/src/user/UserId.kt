package me.nekoalice.mafia.api.dto.user

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@JvmInline
@OptIn(ExperimentalUuidApi::class)
public value class UserId(public val value: Uuid) {
    public constructor(value: String) : this(Uuid.parse(value))

    override fun toString(): String = value.toString()

    public companion object {
        public fun new(): UserId = UserId(Uuid.generateV7())
    }
}
