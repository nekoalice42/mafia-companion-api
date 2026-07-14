package me.nekoalice.mafia.api.contracts.routes

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.contracts.openapi.descriptions.user.UserMeDescriber
import me.nekoalice.mafia.api.contracts.resources.UserResource
import me.nekoalice.mafia.api.contracts.routes.meta.defineRoute
import me.nekoalice.mafia.api.dto.user.UserId

context(routing: Route)
internal fun BaseAPI.applyPrivateUserRoutes() {
    routing.defineRoute<UserResource.Me, _>(Get, UserMeDescriber) {
        getMe(call.principal<UserId>()!!)
    }
}
