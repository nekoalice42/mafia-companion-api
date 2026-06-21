package me.nekoalice.mafia.api.server.storage.base

interface StorageProvider {
    val auth: AuthStorage
    val game: GameStorage
    val player: PlayerStorage
    val tournament: TournamentStorage
    val user: UserStorage

    suspend fun ping()
}
