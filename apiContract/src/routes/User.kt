package me.nekoalice.mafia.api.contracts.routes

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.contracts.openapi.descriptions.DummyDescriber
import me.nekoalice.mafia.api.contracts.openapi.descriptions.user.UserMeDescriber
import me.nekoalice.mafia.api.contracts.resources.UserResource
import me.nekoalice.mafia.api.contracts.routes.meta.defineGroup
import me.nekoalice.mafia.api.contracts.routes.meta.defineRoute
import me.nekoalice.mafia.api.contracts.routes.meta.withUserIdContext
import me.nekoalice.mafia.api.dto.user.InputUser
import me.nekoalice.mafia.api.dto.user.external.ExternalUserInfo
import me.nekoalice.mafia.api.dto.user.security.InputPassword

context(routing: Route)
internal fun BaseAPI.applyPrivateUserRoutes() {
    routing.defineGroup<UserResource>(DummyDescriber.Accept) {
        get { withUserIdContext { listUsers() } }
        post<InputUser, _> { withUserIdContext { createUser(it) } }
    }

    routing.defineGroup<UserResource.ById>(DummyDescriber.Accept) {
        get { res -> withUserIdContext { getUser(res.id) } }
        put<InputUser, _> { res, user -> withUserIdContext { updateUser(res.id, user) } }
        delete { res -> withUserIdContext { deleteUser(res.id) } }
    }

    routing.defineRoute<UserResource.ById.External, _>(Get, DummyDescriber.Accept) { res ->
        withUserIdContext { getExternalUserBinds(res.parent.id) }
    }

    routing.defineGroup<UserResource.ById.External.Provider>(DummyDescriber.Accept) {
        put<ExternalUserInfo, _> { res, info ->
            withUserIdContext { bindExternalUser(res.parent.parent.id, res.provider, info) }
        }
        delete { res ->
            withUserIdContext { unbindExternalUser(res.parent.parent.id, res.provider) }
        }
    }

    routing.defineRoute<UserResource.ById.Password, InputPassword, _>(
        Put,
        DummyDescriber.Accept,
    ) { res, password -> withUserIdContext { updateUserPassword(res.parent.id, password) } }

    routing.defineRoute<UserResource.Me, _>(Get, UserMeDescriber) {
        withUserIdContext { getMe() }
    }
}
