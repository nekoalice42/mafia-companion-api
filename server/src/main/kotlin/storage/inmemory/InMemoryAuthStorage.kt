package me.nekoalice.mafia.api.server.storage.inmemory

import me.nekoalice.mafia.api.dto.user.UserId
import me.nekoalice.mafia.api.server.storage.TokenPair
import me.nekoalice.mafia.api.server.storage.adminUserDefaultPassword
import me.nekoalice.mafia.api.server.storage.adminUserUuid
import me.nekoalice.mafia.api.server.storage.base.AuthStorage
import me.nekoalice.mafia.api.server.utils.generateToken
import me.nekoalice.mafia.api.server.utils.hashPassword
import me.nekoalice.mafia.api.server.utils.hashPasswordSuspend
import me.nekoalice.mafia.api.server.utils.hashToken
import me.nekoalice.mafia.api.server.utils.verifyPasswordSuspend
import kotlin.time.Duration
import kotlin.time.Instant

class InMemoryAuthStorage : AuthStorage {
    private val passwords = mutableMapOf(
        UserId(adminUserUuid) to hashPassword(adminUserDefaultPassword)
    )
    private val tokenPairs = mutableMapOf<UserId, TokenPair>()

    override suspend fun setPassword(id: UserId, password: String) {
        passwords[id] = hashPasswordSuspend(password)
    }

    override suspend fun verifyPassword(id: UserId, password: String): Boolean =
        passwords[id]?.let { verifyPasswordSuspend(password, it) } ?: false

    override suspend fun recreateTokenPair(
        userId: UserId,
        currentTime: Instant,
        accessTokenExpiration: Duration,
        refreshTokenExpiration: Duration,
    ): TokenPair {
        val tokenPair = TokenPair(
            access = generateToken(),
            refresh = generateToken(),
        )
        tokenPairs[userId] = tokenPair.copy(
            refresh = hashToken(tokenPair.refresh),
        )
        return tokenPair
    }

    override suspend fun verifyAccessTokenOrNull(accessToken: String): UserId? =
        tokenPairs.entries.find { it.value.access == accessToken }?.key

    override suspend fun verifyRefreshTokenOrNull(refreshToken: String): UserId? =
        hashToken(refreshToken).let { hashed ->
            tokenPairs.entries.find { it.value.refresh == hashed }?.key
        }

    override suspend fun revokeTokens(userId: UserId) {
        tokenPairs.remove(userId)
    }
}
