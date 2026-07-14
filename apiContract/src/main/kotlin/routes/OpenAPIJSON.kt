package me.nekoalice.mafia.api.contracts.routes

import io.ktor.openapi.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import kotlinx.serialization.json.Json
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.contracts.BaseAPI.Response
import me.nekoalice.mafia.api.contracts.resources.OpenAPIJSONResource
import me.nekoalice.mafia.api.contracts.routes.meta.defineRoute

context(routing: Route)
internal fun BaseAPI.applyPublicOpenAPIJSONRoutes() {
    routing.defineRoute<OpenAPIJSONResource, _>(Get, null) {
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
