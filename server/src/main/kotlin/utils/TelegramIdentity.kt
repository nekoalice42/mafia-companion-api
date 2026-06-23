package me.nekoalice.mafia.api.server.utils

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import java.security.interfaces.RSAPublicKey

private val jwkProvider = JwkProviderBuilder("oauth.telegram.org").build()

private inline fun <reified T> requireIs(value: Any, lazyMessage: () -> String): T {
    require(value is T, lazyMessage)
    return value
}

data class TelegramIdentity(
    val id: Long,
    val username: String?,
)

fun parseAndVerifyTelegramToken(token: String, clientId: String): Result<TelegramIdentity> {
    val data = JWT.decode(token)
    val pubkey = try {
        require(data.algorithm == "RS256") { "Unexpected algorithm" }
        jwkProvider
            .get(requireNotNull(data.keyId) { "Missing key ID" })
            .publicKey
            .let { requireIs<RSAPublicKey>(it) { "Unexpected key type" } }
    } catch (e: IllegalArgumentException) {
        return Result.failure(e)
    }
    try {
        JWT.require(Algorithm.RSA256(pubkey, null))
            .withIssuer("https://oauth.telegram.org")
            .withAudience(clientId)
            .build()
            .verify(data)
    } catch (e: JWTVerificationException) {
        return Result.failure(e)
    }
    val identity = try {
        val telegramId = requireNotNull(
            data.getClaim("id").asString()?.toLong(),
        ) { "Missing id claim" }
        val username = data.getClaim("preferred_username").asString()
        TelegramIdentity(
            id = telegramId,
            username = username,
        )
    } catch (e: IllegalArgumentException) {
        return Result.failure(e)
    }
    return Result.success(identity)
}
