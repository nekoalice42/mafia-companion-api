package me.nekoalice.mafia.api.cleaner

import me.nekoalice.mafia.api.dao.AccessTokens
import me.nekoalice.mafia.api.dao.AuthCodes
import me.nekoalice.mafia.api.dao.ClientStates
import me.nekoalice.mafia.api.dao.RefreshTokens
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Clock

val logger: Logger = LoggerFactory.getLogger("Cleaner")

fun env(key: String): String =
    System.getenv(key) ?: throw IllegalArgumentException("Environment variable $key is not set")

suspend fun main() {
    R2dbcDatabase.connect(
        url = "r2dbc:" + env("DATABASE_URL"),
        driver = "postgresql",
        user = env("DATABASE_USER"),
        password = env("DATABASE_PASSWORD"),
    )
    val now = Clock.System.now()
    logger.info("Started cleaning object expired at {}", now.toString())
    val totalRemoved = suspendTransaction {
        val accessTokens = AccessTokens.deleteWhere { AccessTokens.expiresAt less now }
        logger.debug("Removed {} expired access tokens", accessTokens)
        val authCodes = AuthCodes.deleteWhere { AuthCodes.expiresAt less now }
        logger.debug("Removed {} expired auth codes", authCodes)
        val clientStates = ClientStates.deleteWhere { ClientStates.expiresAt less now }
        logger.debug("Removed {} expired client states", clientStates)
        val refreshTokens = RefreshTokens.deleteWhere { RefreshTokens.expiresAt less now }
        logger.debug("Removed {} expired refresh tokens", refreshTokens)

        accessTokens + authCodes + clientStates + refreshTokens
    }
    logger.info("Removed {} expired objects", totalRemoved)
}
