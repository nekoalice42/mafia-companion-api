package me.nekoalice.mafia.api.dao

import org.jetbrains.exposed.v1.core.Table

internal inline fun <reified T : Enum<T>> Table.enumerationByName(name: String) =
    text(name, eagerLoading = true).transform(wrap = { enumValueOf<T>(it) }, unwrap = { it.name })
