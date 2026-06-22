package me.nekoalice.mafia.api.server.storage.base

import me.nekoalice.mafia.api.dto.player.Player
import me.nekoalice.mafia.api.dto.player.PlayerId

interface PlayerStorage : CRUDStorage<Player, PlayerId>
