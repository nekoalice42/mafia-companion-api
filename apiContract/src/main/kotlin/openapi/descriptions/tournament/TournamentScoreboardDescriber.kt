package me.nekoalice.mafia.api.contracts.openapi.descriptions.tournament

import io.ktor.http.HttpMethod
import io.ktor.openapi.Operation
import io.ktor.openapi.jsonSchema
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIResourceDescriber
import me.nekoalice.mafia.api.contracts.openapi.descriptions.errorResponse
import me.nekoalice.mafia.api.contracts.openapi.descriptions.successResponseOf
import me.nekoalice.mafia.api.contracts.openapi.descriptions.unsupportedMethod
import me.nekoalice.mafia.api.dto.response.ResponseList
import me.nekoalice.mafia.api.dto.tournament.TournamentId
import me.nekoalice.mafia.api.dto.tournament.scoreboard.ScoreboardRow

internal object TournamentScoreboardDescriber : OpenAPIResourceDescriber {
    override val supportedMethods: Set<HttpMethod> =
        setOf(Get)

    override fun describe(
        method: HttpMethod,
        builder: Operation.Builder,
    ) {
        if (method !in supportedMethods) unsupportedMethod(method)
        with(builder) {
            parameters {
                path("tournament_id") {
                    description = "Tournament ID to retrieve scoreboard for"
                    schema = jsonSchema<TournamentId>()
                }
            }
            responses {
                successResponseOf<ResponseList<ScoreboardRow>>("Tournament scoreboard")
                errorResponse(NotFound, "Tournament not found")
            }
        }
    }
}
