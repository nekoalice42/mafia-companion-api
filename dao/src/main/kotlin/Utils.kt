package me.nekoalice.mafia.api.dao

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone
import java.time.ZoneOffset
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

internal inline fun <reified T : Enum<T>> Table.enumerationByName(name: String) =
    text(name, eagerLoading = true).transform(wrap = { enumValueOf<T>(it) }, unwrap = { it.name })

internal fun Table.timestampWithTimeZoneAsInstant(name: String) =
    timestampWithTimeZone(name).transform(
        wrap = { it.toInstant().toKotlinInstant() },
        unwrap = { it.toJavaInstant().atOffset(ZoneOffset.UTC) },
    )
