package me.nekoalice.mafia.api.contracts.routes

import io.ktor.openapi.OpenApiDoc
import io.ktor.openapi.OpenApiInfo
import io.ktor.openapi.Server
import io.ktor.server.routing.Route
import io.ktor.server.routing.openapi.plus
import io.ktor.server.routing.routingRoot
import kotlinx.serialization.json.Json
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.contracts.BaseAPI.Response
import me.nekoalice.mafia.api.contracts.resources.OpenAPIJSONResource
import me.nekoalice.mafia.api.contracts.routes.meta.define

context(routing: Route)
internal fun BaseAPI.applyPublicOpenAPIJSONRoutes() {
    routing.define<OpenAPIJSONResource, _>(Get, null) {
        val doc = OpenApiDoc(
            info = OpenApiInfo(
                title = info.name,
                version = info.version,
                license = info.licenseIdentifier?.let {
                    OpenApiInfo.License(
                        name = it,
                        identifier = it,
                    )
                },
            ),
            servers = listOfNotNull(
                info.developmentUrl?.let {
                    Server(url = it, description = "Development server")
                },
                info.stagingUrl?.let {
                    Server(url = it, description = "Staging server")
                },
                info.productionUrl?.let {
                    Server(url = it, description = "Production server")
                },
            ),
        ) + call.application.routingRoot.descendants()
        val encodedDoc = openapiJsonConfig.encodeToString(doc)
        Response.Raw.rawJson(encodedDoc)
    }
}

private val openapiJsonConfig = Json {
    encodeDefaults = false
}
