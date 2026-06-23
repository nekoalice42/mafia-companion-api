package me.nekoalice.mafia.api.migrations

import kotlinx.coroutines.flow.single
import me.nekoalice.mafia.api.migrations.migrations.*
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.r2dbc.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val migrations = listOf(
    V01Init,
    V02AddSeatColumn,
    V03TimestampToTimestampTz,
    V04AddTokensAndPlayers,
    V05AddExternalUsers,
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

context(_: R2dbcTransaction)
suspend fun migrationUp(migration: Migration) {
    migration.up()
    MigrationsTable.versionUp()
    logger.info("Migration ${migration::class.simpleName} applied")
}

context(_: R2dbcTransaction)
suspend fun migrationDown(migration: Migration) {
    migration.down()
    MigrationsTable.versionDown()
    logger.info("Migration ${migration::class.simpleName} reverted")
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
    logger.debug("Current version: $currentVersion")

    val action = if (args.isEmpty()) "latest" else args[0]
    logger.debug("Args: '{}'", args)
    logger.debug("Action: $action")
    when (action) {
        "latest" -> runMigrations(
            migrations
                .filter { it.version > currentVersion }
                .sortedBy { it.version },
        ) { migrationUp(it) }

        "clear" -> runMigrations(
            migrations
                .filter { it.version <= currentVersion }
                .sortedByDescending { it.version },
        ) { migrationDown(it) }

        "upto" -> args.getOrNull(1)?.toUInt()?.let { targetVersion ->
            runMigrations(
                migrations
                    .filter { it.version in (currentVersion + 1u)..targetVersion }
                    .sortedBy { it.version },
            ) { migrationUp(it) }
        } ?: throw IllegalArgumentException("Target version must be specified")

        "downto" -> args.getOrNull(1)?.toUInt()?.let { targetVersion ->
            runMigrations(
                migrations
                    .filter { it.version in currentVersion downTo (targetVersion + 1u) }
                    .sortedByDescending { it.version },
            ) { migrationDown(it) }
        } ?: throw IllegalArgumentException("Target version must be specified")

        else -> throw IllegalArgumentException("Invalid action: $action")
    }
}
