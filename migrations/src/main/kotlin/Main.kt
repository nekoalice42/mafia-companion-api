package me.nekoalice.mafia.api.migrations

import me.nekoalice.mafia.api.migrations.migrations.V01Init
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

// must be ordered by version!
val migrations = listOf(
    V01Init,
)

fun env(key: String): String =
    System.getenv(key) ?: throw IllegalArgumentException("Environment variable $key is not set")

suspend fun main(args: Array<String>) {
    R2dbcDatabase.connect(
        url = "r2dbc:" + env("DATABASE_URL"),
        driver = "postgresql",
        user = env("DATABASE_USER"),
        password = env("DATABASE_PASSWORD"),
    )

    val action = if (args.isEmpty()) "latest" else args[0]
    // TODO: add up and down by X
    when (action) {
        "latest" -> migrations.forEach {
            suspendTransaction {
                context(this) {
                    it.up()
                }
            }
        }

        "first" -> migrations.asReversed().forEach {
            suspendTransaction {
                context(this) {
                    it.down()
                }
            }
        }

        else -> throw IllegalArgumentException("Invalid action: $action")
    }
}
