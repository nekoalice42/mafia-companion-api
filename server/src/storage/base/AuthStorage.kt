package me.nekoalice.mafia.api.server.storage.base

import me.nekoalice.mafia.api.dto.user.UserId
import me.nekoalice.mafia.api.server.storage.TokenPair
import kotlin.time.Duration
import kotlin.time.Instant

interface AuthStorage {
    suspend fun setPassword(id: UserId, password: String)
    suspend fun verifyPassword(id: UserId, password: String): Boolean
    suspend fun createTokenPair(
        userId: UserId,
        currentTime: Instant,
        accessTokenExpiration: Duration,
        refreshTokenExpiration: Duration,
    ): TokenPair

    suspend fun verifyAccessTokenOrNull(accessToken: String, currentTime: Instant): UserId?
    suspend fun verifyRefreshTokenOrNull(refreshToken: String, currentTime: Instant): UserId?
    suspend fun revokeRefreshToken(refreshToken: String)
    suspend fun revokeTokens(userId: UserId)
    suspend fun setClientState(
        state: String,
        clientState: ClientState,
        currentTime: Instant,
        expiration: Duration,
    )
    suspend fun popClientStateOrNull(state: String, currentTime: Instant): ClientState?
    suspend fun setUserForAuthCode(
        code: String,
        userId: UserId,
        currentTime: Instant,
        expiration: Duration,
    )
    suspend fun popUserForAuthCodeOrNull(code: String, currentTime: Instant): UserId?

    data class ClientState(
        val redirectUrl: String?,
        val state: String?,
    )
}
