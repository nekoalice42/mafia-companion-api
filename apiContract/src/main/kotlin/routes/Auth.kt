package me.nekoalice.mafia.api.contracts.routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.contracts.BaseAPI.Response
import me.nekoalice.mafia.api.contracts.CustomHttpHeaders
import me.nekoalice.mafia.api.contracts.openapi.descriptions.auth.*
import me.nekoalice.mafia.api.contracts.resources.AuthResource
import me.nekoalice.mafia.api.contracts.routes.meta.defineGroup
import me.nekoalice.mafia.api.contracts.routes.meta.defineRoute
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
    routing.defineGroup<AuthResource.Token>(AuthTokenDescriber) {
        post { body: LoginData -> login(body) }
        patch {
            call.request.headers[CustomHttpHeaders.XRefreshToken]
                ?.let { refreshLogin(RefreshToken(it)) }
                ?: Response.Error(
                    message = "Refresh token is missing",
                    statusCode = Unauthorized,
                )
        }
    }

    routing.defineRoute<AuthResource.Telegram.Challenge, ExternalAuthCode, _>(
        Post,
        AuthTelegramChallengeDescriber,
    ) { finishTelegramChallenge(it) }
}

context(routing: Route)
internal fun BaseAPI.applyPrivateAuthRoutes() {
    routing.defineRoute<AuthResource.Password, LoginData, _>(Put, AuthPasswordDescriber) {
        changePassword(it)
    }

    routing.defineRoute<AuthResource.Token, _>(Delete, AuthTokenDescriber) {
        logoutAll(call.principal<UserId>()!!)
    }
}

context(routing: Route)
internal fun BaseAPI.applyTelegramOauthRoutes(isConfigured: Boolean) {
    routing.defineRoute<AuthResource.Telegram.Login, Unit>(Get, AuthTelegramLoginDescriber) {
        // This route exists only to start OAuth2 login flow, it does nothing
        if (!isConfigured)
            telegramOauthNotAvailable
        else
            Response.Error("Assertion error", InternalServerError)
    }

    routing.defineGroup<AuthResource.Telegram.OauthCallback>(AuthTelegramOauthCallbackDescriber) {
        if (!isConfigured) {
            get { telegramOauthNotAvailable }
            return@defineGroup
        }

        accept(ContentType.Text.Html) {
            // [me.nekoalice.mafia.api.contracts.routes.meta.RouteGroup.get] is not available here
            // TODO: investigate a workaround
            get {
                telegramOauthCommon(BaseAPI::telegramOauthCallbackHtml)
                    .sendInResponseTo(call)
            }
        }

        get {
            telegramOauthCommon(BaseAPI::telegramOauthCallback)
        }
    }
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

private val telegramOauthNotAvailable =
    Response.Error<Unit>("Telegram OAuth2 is not available", ServiceUnavailable)
