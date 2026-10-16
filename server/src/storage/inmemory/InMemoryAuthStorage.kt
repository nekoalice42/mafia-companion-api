package me.nekoalice.mafia.api.server.storage.inmemory

import me.nekoalice.mafia.api.dto.user.UserId
import me.nekoalice.mafia.api.server.storage.TokenPair
import me.nekoalice.mafia.api.server.storage.adminUserDefaultPassword
import me.nekoalice.mafia.api.server.storage.adminUserUuid
import me.nekoalice.mafia.api.server.storage.base.AuthStorage
import me.nekoalice.mafia.api.server.utils.*
import kotlin.time.Duration
import kotlin.time.Instant

class InMemoryAuthStorage : AuthStorage {
    private val passwords = mutableMapOf(
        UserId(adminUserUuid) to hashPassword(adminUserDefaultPassword),
    )
    private val tokenPairs = mutableListOf<HashedTokenPairWithUser>()
    private val clientStates = mutableMapOf<String, AuthStorage.ClientState>()
    private val userForAuthCode = mutableMapOf<String, UserId>()

    override suspend fun setPassword(id: UserId, password: String) {
        passwords[id] = hashPasswordSuspend(password)
    }

    override suspend fun verifyPassword(id: UserId, password: String): Boolean =
        passwords[id]?.let { verifyPasswordSuspend(password, it) } ?: false

    override suspend fun createTokenPair(
        userId: UserId,
        currentTime: Instant,
        accessTokenExpiration: Duration,
        refreshTokenExpiration: Duration,
    ): TokenPair {
        val tokenPair = TokenPair(
            access = generateToken(),
            refresh = generateToken(),
        )
        tokenPairs.add(
            HashedTokenPairWithUser(
                tokenPair = tokenPair,
                userId = userId,
            ),
        )
        return tokenPair
    }

    override suspend fun verifyAccessTokenOrNull(
        accessToken: String,
        currentTime: Instant,
    ): UserId? =
        tokenPairs.find { it.checkAccessToken(accessToken) }?.userId

    override suspend fun verifyRefreshTokenOrNull(
        refreshToken: String,
        currentTime: Instant,
    ): UserId? =
        tokenPairs.find { it.checkRefreshToken(refreshToken) }?.userId

    override suspend fun revokeRefreshToken(refreshToken: String) {
        tokenPairs.removeAll { it.checkRefreshToken(refreshToken) }
    }

    override suspend fun revokeTokens(userId: UserId) {
        tokenPairs.removeAll { it.userId == userId }
    }

    override suspend fun setClientState(
        state: String,
        clientState: AuthStorage.ClientState,
        currentTime: Instant,
        expiration: Duration,
    ) {
        clientStates[state] = clientState
    }

    override suspend fun popClientStateOrNull(
        state: String,
        currentTime: Instant,
    ): AuthStorage.ClientState? = clientStates.remove(state)

    override suspend fun setUserForAuthCode(
        code: String,
        userId: UserId,
        currentTime: Instant,
        expiration: Duration,
    ) {
        userForAuthCode[code] = userId
    }

    override suspend fun popUserForAuthCodeOrNull(code: String, currentTime: Instant): UserId? =
        userForAuthCode.remove(code)

    private data class HashedTokenPairWithUser(
        val access: String,
        val refresh: String,
        val userId: UserId,
    ) {
        constructor(tokenPair: TokenPair, userId: UserId) : this(
            access = hashToken(tokenPair.access),
            refresh = hashToken(tokenPair.refresh),
            userId = userId,
        )

        fun checkAccessToken(accessToken: String): Boolean =
            access == hashToken(accessToken)

        fun checkRefreshToken(refreshToken: String): Boolean =
            refresh == hashToken(refreshToken)
    }
}
