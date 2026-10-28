package me.nekoalice.mafia.api.server.storage.inmemory

import me.nekoalice.mafia.api.dto.player.PlayerId
import me.nekoalice.mafia.api.server.storage.base.PlayerStorage

class InMemoryPlayerStorage : PlayerStorage, PlayerMemoryStorage(PlayerId::new)
