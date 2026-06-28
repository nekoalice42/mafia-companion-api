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
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.*
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

// FIXME: `Clock.System.now()` -> `currentTime`
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
        tx {
            AccessTokens.upsert(AccessTokens.userId) {
                it[AccessTokens.userId] = userId.value
                it[AccessTokens.hash] = hashToken(tokenPair.access)
                it[AccessTokens.expiresAt] = Clock.System.now() + accessTokenExpiration
            }
            RefreshTokens.upsert(RefreshTokens.userId) {
                it[RefreshTokens.userId] = userId.value
                it[RefreshTokens.hash] = hashToken(tokenPair.refresh)
                it[RefreshTokens.expiresAt] = Clock.System.now() + refreshTokenExpiration
            }
        }
        return tokenPair
    }

    override suspend fun verifyAccessTokenOrNull(accessToken: String): UserId? =
        readonlyTx {
            AccessTokens.select(AccessTokens.userId, AccessTokens.expiresAt)
                .where { AccessTokens.hash eq hashToken(accessToken) }
                .singleOrNull()
        }
            ?.takeIf { it[AccessTokens.expiresAt] > Clock.System.now() }
            ?.let { UserId(it[AccessTokens.userId].value) }

    override suspend fun verifyRefreshTokenOrNull(refreshToken: String): UserId? =
        readonlyTx {
            RefreshTokens.select(RefreshTokens.userId, RefreshTokens.expiresAt)
                .where { RefreshTokens.hash eq hashToken(refreshToken) }
                .singleOrNull()
        }
            ?.takeIf { it[RefreshTokens.expiresAt] > Clock.System.now() }
            ?.let { UserId(it[RefreshTokens.userId].value) }

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

    override suspend fun popClientStateOrNull(state: String): AuthStorage.ClientState? =
        tx {
            ClientStates.deleteReturning {
                ClientStates.state eq state
            }.singleOrNull()
        }
            ?.takeIf { it[ClientStates.expiresAt] > Clock.System.now() }
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

    override suspend fun popUserForAuthCodeOrNull(code: String): UserId? =
        tx {
            AuthCodes.deleteReturning { AuthCodes.code eq hashToken(code) }.singleOrNull()
        }
            ?.takeIf { it[AuthCodes.expiresAt] > Clock.System.now() }
            ?.let { UserId(it[AuthCodes.userId].value) }
}
