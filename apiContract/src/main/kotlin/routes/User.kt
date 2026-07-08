package me.nekoalice.mafia.api.contracts.routes

import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.contracts.openapi.descriptions.user.UserMeDescriber
import me.nekoalice.mafia.api.contracts.resources.UserResource
import me.nekoalice.mafia.api.contracts.routes.meta.define
import me.nekoalice.mafia.api.dto.user.UserId

context(routing: Route)
internal fun BaseAPI.applyPrivateUserRoutes() {
    routing.define<UserResource.Me, _>(Get, UserMeDescriber) {
        getMe(call.principal<UserId>()!!)
    }
}
