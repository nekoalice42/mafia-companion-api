package me.nekoalice.mafia.api.server.storage.base

interface StorageProvider {
    val game: GameStorage
    val player: PlayerStorage
    val tournament: TournamentStorage
    suspend fun ping()
}
