package me.nekoalice.mafia.api.dto.player

import kotlinx.serialization.Serializable
import me.nekoalice.mafia.api.dto.base.InputCRUDModel

@Serializable
public data class InputPlayer(
    val nickname: String,
) : InputCRUDModel<Player, PlayerId> {
    override fun toModel(id: PlayerId): Player = Player(
        id = id,
        nickname = nickname,
    )
}
