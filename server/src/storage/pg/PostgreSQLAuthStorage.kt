package me.nekoalice.mafia.api.server.storage.pg

import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import me.nekoalice.mafia.api.dao.*
import me.nekoalice.mafia.api.dto.user.UserId
import me.nekoalice.mafia.api.server.storage.TokenPair
import me.nekoalice.mafia.api.server.storage.base.AuthStorage
import me.nekoalice.mafia.api.server.utils.generateToken
import me.nekoalice.mafia.api.server.utils.hashPasswordSuspend
import me.nekoalice.mafia.api.server.utils.hashToken
import me.nekoalice.mafia.api.server.utils.verifyPasswordSuspend
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.r2dbc.*
import kotlin.time.Duration
import kotlin.time.Instant

class PostgreSQLAuthStorage : AuthStorage {
    override suspend fun setPassword(id: UserId, password: String) {
        val hashedPassword = hashPasswordSuspend(password)
        tx {
            Users.update({ Users.id eq id.value }) {
                it[Users.passwordHash] = hashedPassword
            }
        }
    }

    override suspend fun verifyPassword(id: UserId, password: String): Boolean =
        readonlyTx {
            Users.select(Users.passwordHash)
                .where { Users.id eq id.value }
                .single()[Users.passwordHash]
        }.let { pwhash ->
            if (pwhash != null) verifyPasswordSuspend(password, pwhash) else false
        }

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
        tx {
            AccessTokens.insert {
                it[AccessTokens.userId] = userId.value
                it[AccessTokens.hash] = hashToken(tokenPair.access)
                it[AccessTokens.expiresAt] = currentTime + accessTokenExpiration
            }
            RefreshTokens.insert {
                it[RefreshTokens.userId] = userId.value
                it[RefreshTokens.hash] = hashToken(tokenPair.refresh)
                it[RefreshTokens.expiresAt] = currentTime + refreshTokenExpiration
            }
        }
        return tokenPair
    }

    override suspend fun verifyAccessTokenOrNull(
        accessToken: String,
        currentTime: Instant,
    ): UserId? =
        readonlyTx {
            AccessTokens.select(AccessTokens.userId)
                .where {
                    (AccessTokens.hash eq hashToken(accessToken))
                        .and(AccessTokens.expiresAt greater currentTime)
                }
                .singleOrNull()
        }
            ?.let { UserId(it[AccessTokens.userId].value) }

    override suspend fun verifyRefreshTokenOrNull(
        refreshToken: String,
        currentTime: Instant,
    ): UserId? =
        readonlyTx {
            RefreshTokens.select(RefreshTokens.userId)
                .where {
                    (RefreshTokens.hash eq hashToken(refreshToken))
                        .and(RefreshTokens.expiresAt greater currentTime)
                }
                .singleOrNull()
        }
            ?.let { UserId(it[RefreshTokens.userId].value) }

    override suspend fun revokeRefreshToken(refreshToken: String) {
        tx {
            RefreshTokens.deleteWhere { RefreshTokens.hash eq hashToken(refreshToken) }
        }
    }

    override suspend fun revokeTokens(userId: UserId) {
        tx {
            AccessTokens.deleteWhere { AccessTokens.userId eq userId.value }
            RefreshTokens.deleteWhere { RefreshTokens.userId eq userId.value }
        }
    }

    override suspend fun setClientState(
        state: String,
        clientState: AuthStorage.ClientState,
        currentTime: Instant,
        expiration: Duration,
    ) {
        tx {
            ClientStates.upsert {
                it[ClientStates.state] = state
                it[ClientStates.redirectUrl] = clientState.redirectUrl
                it[ClientStates.clientState] = clientState.state
                it[ClientStates.expiresAt] = currentTime + expiration
            }
        }
    }

    override suspend fun popClientStateOrNull(
        state: String,
        currentTime: Instant,
    ): AuthStorage.ClientState? =
        tx {
            ClientStates.deleteReturning {
                (ClientStates.state eq state) and (ClientStates.expiresAt greater currentTime)
            }.singleOrNull()
        }
            ?.let {
                AuthStorage.ClientState(
                    redirectUrl = it[ClientStates.redirectUrl],
                    state = it[ClientStates.clientState],
                )
            }

    override suspend fun setUserForAuthCode(
        code: String,
        userId: UserId,
        currentTime: Instant,
        expiration: Duration,
    ) {
        tx {
            AuthCodes.upsert {
                it[AuthCodes.code] = hashToken(code)
                it[AuthCodes.userId] = userId.value
                it[AuthCodes.expiresAt] = currentTime + expiration
            }
        }
    }

    override suspend fun popUserForAuthCodeOrNull(code: String, currentTime: Instant): UserId? =
        tx {
            AuthCodes.deleteReturning {
                (AuthCodes.code eq hashToken(code)) and (AuthCodes.expiresAt greater currentTime)
            }.singleOrNull()
        }
            ?.takeIf { it[AuthCodes.expiresAt] > currentTime }
            ?.let { UserId(it[AuthCodes.userId].value) }
}
