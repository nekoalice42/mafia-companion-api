package me.nekoalice.mafia.api.server.storage.base

import me.nekoalice.mafia.api.dto.user.UserId
import me.nekoalice.mafia.api.server.storage.TokenPair
import kotlin.time.Duration
import kotlin.time.Instant

interface AuthStorage {
    suspend fun setPassword(id: UserId, password: String)
    suspend fun verifyPassword(id: UserId, password: String): Boolean
    suspend fun recreateTokenPair(
        userId: UserId,
        currentTime: Instant,
        accessTokenExpiration: Duration,
        refreshTokenExpiration: Duration,
    ): TokenPair

    suspend fun verifyAccessTokenOrNull(accessToken: String): UserId?
    suspend fun verifyRefreshTokenOrNull(refreshToken: String): UserId?
    suspend fun revokeTokens(userId: UserId)
}
