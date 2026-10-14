package me.nekoalice.mafia.api.dto.player

import kotlinx.serialization.Serializable
import me.nekoalice.mafia.api.dto.base.CRUDModel

@Serializable
public data class Player(
    override val id: PlayerId,
    val nickname: String,
) : CRUDModel<PlayerId>
