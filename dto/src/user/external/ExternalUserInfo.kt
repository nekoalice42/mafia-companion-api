package me.nekoalice.mafia.api.dto.user.external

import kotlinx.serialization.Serializable

@Serializable
public data class ExternalUserInfo(
    val externalId: String,
)
