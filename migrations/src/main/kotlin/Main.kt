package me.nekoalice.mafia.api.migrations

import kotlinx.coroutines.flow.single
import me.nekoalice.mafia.api.migrations.migrations.*
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.r2dbc.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// must be ordered by version!
val migrations = listOf(
    V01Init,
)

val logger: Logger = LoggerFactory.getLogger("Migrations")

fun env(key: String): String =
    System.getenv(key) ?: throw IllegalArgumentException("Environment variable $key is not set")

object MigrationsTable : Table("migrations") {
    val version: Column<UInt> = uinteger("version")

    suspend fun versionUp(): UInt = updateReturning {
        it[version] = version + 1u
    }.single()[version]

    suspend fun versionDown(): UInt = updateReturning {
        it[version] = version - 1u
    }.single()[version]

    suspend fun getVersion(): UInt =
        MigrationsTable.select(version).single()[version]

    suspend fun createAndInit(): UInt {
        SchemaUtils.create(MigrationsTable)
        return try {
            getVersion()
        } catch (_: NoSuchElementException) {
            insertReturning {
                it[version] = 0u
            }.single()[version]
        }
    }
}

suspend fun <T> suspendTransactionInContext(
    block: suspend context(R2dbcTransaction) () -> T,
): T = suspendTransaction {
    context(this) {
        block()
    }
}

suspend fun runMigrations(
    requiredMigrations: List<Migration>,
    action: suspend context(R2dbcTransaction) (Migration) -> Unit,
) {
    logger.info("Found ${requiredMigrations.size} pending migrations")
    for (migration in requiredMigrations) {
        suspendTransactionInContext {
            action(migration)
        }
    }
}

suspend fun main(args: Array<String>) {
    R2dbcDatabase.connect(
        url = "r2dbc:" + env("DATABASE_URL"),
        driver = "postgresql",
        user = env("DATABASE_USER"),
        password = env("DATABASE_PASSWORD"),
    )

    val currentVersion: UInt = suspendTransaction {
        MigrationsTable.createAndInit()
    }

    val action = if (args.isEmpty()) "latest" else args[0]
    // TODO: add up and down by X
    when (action) {
        "latest" -> runMigrations(
            migrations
                .filter { it.version > currentVersion }
                .sortedBy { it.version },
        ) {
            it.up()
            MigrationsTable.versionUp()
            logger.info("Migration ${it::class.simpleName} applied")
        }

        "clear" -> runMigrations(
            migrations
                .filter { it.version <= currentVersion }
                .sortedByDescending { it.version },
        ) {
            it.down()
            MigrationsTable.versionDown()
            logger.info("Migration ${it::class.simpleName} reverted")
        }

        else -> throw IllegalArgumentException("Invalid action: $action")
    }
}
