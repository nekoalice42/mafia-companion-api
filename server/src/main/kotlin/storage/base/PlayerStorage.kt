package me.nekoalice.mafia.api.server.storage.base

import me.nekoalice.mafia.api.dto.models.Player
import me.nekoalice.mafia.api.dto.models.PlayerId

interface PlayerStorage : CRUDStorage<Player, PlayerId>
