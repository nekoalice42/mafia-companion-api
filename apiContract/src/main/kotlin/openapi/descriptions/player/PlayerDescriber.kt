package me.nekoalice.mafia.api.contracts.openapi.descriptions.player

import io.ktor.http.HttpMethod
import io.ktor.openapi.Operation
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIResourceDescriber
import me.nekoalice.mafia.api.contracts.openapi.descriptions.*
import me.nekoalice.mafia.api.dto.player.InputPlayer
import me.nekoalice.mafia.api.dto.player.Player
import me.nekoalice.mafia.api.dto.response.NewResourceId
import me.nekoalice.mafia.api.dto.response.ResponseList

internal object PlayerDescriber : OpenAPIResourceDescriber {
    override val supportedMethods: Set<HttpMethod> =
        setOf(Get, Post)

    override fun describe(
        method: HttpMethod,
        builder: Operation.Builder,
    ) = when (method) {
        Get -> describeGet(builder)
        Post -> describePost(builder)
        else -> unsupportedMethod(method)
    }

    private fun describeGet(builder: Operation.Builder) = builder.run {
        responses {
            successResponseOf<ResponseList<Player>>("List of players")
        }
    }

    private fun describePost(builder: Operation.Builder) = builder.run {
        requestBodyOf<InputPlayer>("Player details to create")
        responses {
            successResponseOf<NewResourceId>("Player created", Created)
            defaultKtorBodyErrorResponsesFor<InputPlayer>()
        }
    }
}
