package me.nekoalice.mafia.api.migrations.migrations

import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.core.statements.StatementType
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import kotlin.uuid.Uuid

object V11AddUserRole : Migration {
    override val version = 11u

    context(transaction: R2dbcTransaction)
    override suspend fun up() {
        transaction.exec(
            "alter table users add column role text null",
            explicitStatementType = StatementType.ALTER,
        )
        Users.update(where = { Users.id neq adminId }) {
            it[role] = UserRole.JUDGE.name
        }
        Users.update(where = { Users.id eq adminId }) {
            it[role] = UserRole.ADMIN.name
        }
        transaction.exec(
            "alter table users alter column role set not null",
            explicitStatementType = StatementType.ALTER,
        )
    }

    context(transaction: R2dbcTransaction)
    override suspend fun down() {
        transaction.exec(
            "alter table users drop column role",
        )
    }

    private val adminId = Uuid.parse("019ee53f-bc60-7000-8000-000000000000")

    private object Users : UuidTable("users", uuidVersion = UuidVersion.V7) {
        val role = text("role")
    }

    private enum class UserRole { ADMIN, JUDGE }
}
