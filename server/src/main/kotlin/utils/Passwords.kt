package me.nekoalice.mafia.api.server.utils

import com.password4j.Argon2Function
import com.password4j.Password
import com.password4j.types.Argon2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom

private val random = SecureRandom()
private val argon2Hasher = Argon2Function.getInstance(
    15360,
    2,
    1,
    32,
    Argon2.ID,
    19,
)

fun hashPassword(password: String): String =
    Password.hash(password)
        .addRandomSalt(64)
        .with(argon2Hasher)
        .result

fun verifyPassword(password: String, hash: String): Boolean =
    Password.check(password, hash).with(argon2Hasher)

suspend fun hashPasswordSuspend(password: String): String =
    withContext(Dispatchers.Default) { hashPassword(password) }

suspend fun verifyPasswordSuspend(password: String, hash: String): Boolean =
    withContext(Dispatchers.Default) { verifyPassword(password, hash) }

fun generateToken(): String =
    ByteArray(32).also(random::nextBytes).toHexString()

fun hashToken(token: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(token.encodeToByteArray())
        .toHexString()
