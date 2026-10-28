package me.nekoalice.mafia.api.contracts.openapi.descriptions

import io.ktor.http.HttpMethod
import io.ktor.openapi.Operation
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIResourceDescriber

public object DummyDescriber {
    public object Reject : OpenAPIResourceDescriber {
        override val supportedMethods: Set<HttpMethod> = emptySet()

        override fun describe(method: HttpMethod, builder: Operation.Builder): Nothing =
            unsupportedMethod(method)
    }

    public object Accept : OpenAPIResourceDescriber {
        override val supportedMethods: Set<HttpMethod> = HttpMethod.DefaultMethods.toSet()

        override fun describe(method: HttpMethod, builder: Operation.Builder) {
            if (method !in supportedMethods) unsupportedMethod(method)
        }
    }
}
