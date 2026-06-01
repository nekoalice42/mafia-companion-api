package me.nekoalice.mafia.api.dto.models

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
public value class TournamentId(public val value: String)
