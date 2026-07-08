package me.nekoalice.mafia.api.contracts.openapi

import io.ktor.http.HttpMethod
import io.ktor.openapi.Operation

public interface OpenAPIRouteDescriber {
    public val supportedMethods: Set<HttpMethod>

    public fun describe(method: HttpMethod, builder: Operation.Builder)
}
