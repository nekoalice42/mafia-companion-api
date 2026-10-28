package me.nekoalice.mafia.api.contracts.routes.meta

import io.ktor.server.auth.principal
import io.ktor.server.routing.RoutingContext
import me.nekoalice.mafia.api.dto.user.UserId

internal suspend inline fun <R> RoutingContext.withUserIdContext(
    block: suspend context(UserId) () -> R,
): R = context(call.principal<UserId>()!!) { block() }
