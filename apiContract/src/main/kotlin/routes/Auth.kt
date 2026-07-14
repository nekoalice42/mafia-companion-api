package me.nekoalice.mafia.api.contracts.routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.accept
import io.ktor.server.request.acceptItems
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

        get {
            val acceptsHtml = call.request.acceptItems().find {
                runCatching { ContentType.Text.Html.match(it.value) }
                    .getOrDefault(false)
            }
            if (call.request.accept() == null && acceptsHtml == null) {
                return@get Response.Error("Client doesn't accept HTML", NotAcceptable)
            }

            call.principal<OAuthAccessTokenResponse.OAuth2>()?.let { principal ->
                principal.extraParameters["id_token"]?.let { token ->
                    principal.state?.let { state ->
                        telegramOauthCallbackHtml(TelegramIdToken(token), state)
                    }
                }
            } ?: Response.Error(
                "Telegram login flow failed",
                HttpStatusCode.ServiceUnavailable,
            )
        }
    }
}

private val telegramOauthNotAvailable =
    Response.Error<Unit>("Telegram OAuth2 is not available", ServiceUnavailable)
