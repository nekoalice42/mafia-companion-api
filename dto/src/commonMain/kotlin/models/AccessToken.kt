package me.nekoalice.mafia.api.dto.models

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
public value class AccessToken(public val value: String)
