package me.nekoalice.mafia.api.contracts.routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.contracts.BaseAPI.Response
import me.nekoalice.mafia.api.contracts.CustomHttpHeaders
import me.nekoalice.mafia.api.contracts.openapi.descriptions.auth.*
import me.nekoalice.mafia.api.contracts.resources.AuthResource
import me.nekoalice.mafia.api.contracts.routes.meta.define
import me.nekoalice.mafia.api.dto.auth.ExternalAuthCode
import me.nekoalice.mafia.api.dto.auth.LoginData
import me.nekoalice.mafia.api.dto.auth.RefreshToken
import me.nekoalice.mafia.api.dto.auth.TelegramIdToken
import me.nekoalice.mafia.api.dto.user.UserId
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

context(routing: Route)
internal fun BaseAPI.applyPublicAuthRoutes() {
    routing.define<AuthResource.Token, LoginData, _>(Post, AuthTokenDescriber) {
        login(it)
    }

    routing.define<AuthResource.Token, _>(Patch, AuthTokenDescriber) {
        val refreshToken = call.request.headers[CustomHttpHeaders.XRefreshToken]
            ?: return@define Response.Error(
                message = "Refresh token is missing",
                statusCode = Unauthorized,
            )
        refreshLogin(RefreshToken(refreshToken))
    }

    routing.define<AuthResource.Telegram.Challenge, ExternalAuthCode, _>(
        Post,
        AuthTelegramChallengeDescriber,
    ) {
        finishTelegramChallenge(it)
    }
}

context(routing: Route)
internal fun BaseAPI.applyPrivateAuthRoutes() {
    routing.define<AuthResource.Password, LoginData, _>(Put, AuthPasswordDescriber) {
        changePassword(it)
    }

    routing.define<AuthResource.Token, _>(Delete, AuthTokenDescriber) {
        logoutAll(call.principal<UserId>()!!)
    }
}

context(routing: Route)
internal fun BaseAPI.applyTelegramOauthRoutes() {
    routing.define<AuthResource.Telegram.Login, Unit>(Get, AuthTelegramLoginDescriber) {
        // This route exists only to start OAuth2 login flow, it does nothing
        Response.Error("Assertion error", InternalServerError)
    }

    // TODO: use `define`
    @OptIn(ExperimentalKtorApi::class)
    routing.resource<AuthResource.Telegram.OauthCallback> {
        accept(ContentType.Text.Html) {
            get {
                telegramOauthCommon(BaseAPI::telegramOauthCallbackHtml).sendInResponseTo(call)
            }
        }

        get {
            telegramOauthCommon(BaseAPI::telegramOauthCallback).sendInResponseTo(call)
        }
    }.describe { AuthTelegramOauthCallbackDescriber.describe(Get, this) }

}

@OptIn(ExperimentalContracts::class)
context(ctx: RoutingContext)
private suspend fun <RT : Any> BaseAPI.telegramOauthCommon(
    next: suspend BaseAPI.(TelegramIdToken, String) -> Response<RT>,
): Response<RT> {
    contract {
        callsInPlace(next, InvocationKind.AT_MOST_ONCE)
        returnsResultOf(next)
    }
    val error = Response.Error<RT>(
        "Telegram login flow failed",
        HttpStatusCode.ServiceUnavailable,
    )
    val principal = ctx.call.principal<OAuthAccessTokenResponse.OAuth2>() ?: return error
    val token = principal.extraParameters["id_token"] ?: return error
    val state = principal.state ?: return error
    return next(TelegramIdToken(token), state)
}
