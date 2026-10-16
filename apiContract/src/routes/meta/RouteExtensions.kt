package me.nekoalice.mafia.api.contracts.routes.meta

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIResourceDescriber

internal typealias MethodHandler1<P, R> = suspend RoutingContext.(P) -> BaseAPI.Response<R>
internal typealias MethodHandler2<P1, P2, R> = suspend RoutingContext.(P1, P2) -> BaseAPI.Response<R>

internal fun OpenAPIResourceDescriber.assertMethodSupported(method: HttpMethod) {
    require(method in supportedMethods) { "Method $method is not supported by describer" }
}

@IgnorableReturnValue
@ExperimentalKtorApi
internal fun Route.describeOrHide(describer: OpenAPIResourceDescriber?, method: HttpMethod): Route =
    if (describer != null)
        describe { describer.describe(method, this) }
    else hide()

@OptIn(ExperimentalKtorApi::class)
internal class RouteGroup<ResourceT : Any>(
    private val route: Route,
    private val resourceSerializer: KSerializer<ResourceT>,
    private val describer: OpenAPIResourceDescriber?,
): Route by route {
    private fun createHandler(
        method: HttpMethod,
        body: suspend RoutingContext.(ResourceT) -> Unit,
    ): Route {
        describer?.assertMethodSupported(method)

        return route.method(method) { handle(resourceSerializer, body) }
            .describeOrHide(describer, method)
    }

    @IgnorableReturnValue
    inline fun <reified BodyT, reified ResponseT : Any> method(
        method: HttpMethod,
        crossinline body: MethodHandler2<ResourceT, BodyT, ResponseT>,
    ): Route = createHandler(method) { resource ->
        body(resource, call.receive()).sendInResponseTo(call)
    }

    @IgnorableReturnValue
    @JvmName("methodBody")
    inline fun <reified BodyT, reified ResponseT : Any> method(
        method: HttpMethod,
        crossinline body: MethodHandler1<BodyT, ResponseT>,
    ): Route = createHandler(method) {
        body(call.receive()).sendInResponseTo(call)
    }

    @IgnorableReturnValue
    @JvmName("methodResource")
    inline fun <reified ResponseT : Any> method(
        method: HttpMethod,
        crossinline body: MethodHandler1<ResourceT, ResponseT>,
    ): Route = createHandler(method) { resource ->
        body(resource).sendInResponseTo(call)
    }

    @IgnorableReturnValue
    inline fun <reified ResponseT : Any> get(
        crossinline body: MethodHandler1<ResourceT, ResponseT>,
    ): Route = method(Get, body)

    @IgnorableReturnValue
    inline fun <reified ResponseT : Any> delete(
        crossinline body: MethodHandler1<ResourceT, ResponseT>,
    ): Route = method(Delete, body)

    @IgnorableReturnValue
    @JvmName("postResource")
    inline fun <reified ResponseT : Any> post(
        crossinline body: MethodHandler1<ResourceT, ResponseT>,
    ): Route = method(Post, body)

    @IgnorableReturnValue
    inline fun <reified BodyT, reified ResponseT : Any> post(
        crossinline body: MethodHandler2<ResourceT, BodyT, ResponseT>,
    ): Route = method(Post, body)

    @IgnorableReturnValue
    @JvmName("postBody")
    inline fun <reified BodyT, reified ResponseT : Any> post(
        crossinline body: MethodHandler1<BodyT, ResponseT>,
    ): Route = method(Post, body)

    @IgnorableReturnValue
    @JvmName("putResource")
    inline fun <reified ResponseT : Any> put(
        crossinline body: MethodHandler1<ResourceT, ResponseT>,
    ): Route = method(Put, body)

    @IgnorableReturnValue
    inline fun <reified BodyT, reified ResponseT : Any> put(
        crossinline body: MethodHandler2<ResourceT, BodyT, ResponseT>,
    ): Route = method(Put, body)

    @IgnorableReturnValue
    @JvmName("putBody")
    inline fun <reified BodyT, reified ResponseT : Any> put(
        crossinline body: MethodHandler1<BodyT, ResponseT>,
    ): Route = method(Put, body)

    @IgnorableReturnValue
    @JvmName("patchResource")
    inline fun <reified ResponseT : Any> patch(
        crossinline body: MethodHandler1<ResourceT, ResponseT>,
    ): Route = method(Patch, body)

    @IgnorableReturnValue
    inline fun <reified BodyT, reified ResponseT : Any> patch(
        crossinline body: MethodHandler2<ResourceT, BodyT, ResponseT>,
    ): Route = method(Patch, body)

    @IgnorableReturnValue
    @JvmName("patchBody")
    inline fun <reified BodyT, reified ResponseT : Any> patch(
        crossinline body: MethodHandler1<BodyT, ResponseT>,
    ): Route = method(Patch, body)
}

@IgnorableReturnValue
internal inline fun <reified ResourceT : Any> Route.defineGroup(
    describer: OpenAPIResourceDescriber?,
    crossinline body: RouteGroup<ResourceT>.() -> Unit,
): Route = resource<ResourceT> {
    RouteGroup<ResourceT>(this, serializer(), describer).apply(body)
}

@IgnorableReturnValue
@JvmName("defineRouteResource")
internal inline fun <reified ResourceT : Any, reified ResponseT : Any> Route.defineRoute(
    method: HttpMethod,
    describer: OpenAPIResourceDescriber?,
    crossinline body: MethodHandler1<ResourceT, ResponseT>,
): Route = defineGroup<ResourceT>(describer) { method(method, body) }

@IgnorableReturnValue
internal inline fun <reified ResourceT : Any, reified BodyT, reified ResponseT : Any> Route.defineRoute(
    method: HttpMethod,
    describer: OpenAPIResourceDescriber?,
    crossinline body: MethodHandler2<ResourceT, BodyT, ResponseT>,
): Route = defineGroup<ResourceT>(describer) { method(method, body) }

@IgnorableReturnValue
@JvmName("defineRouteBody")
internal inline fun <reified ResourceT : Any, reified BodyT, reified ResponseT : Any> Route.defineRoute(
    method: HttpMethod,
    describer: OpenAPIResourceDescriber?,
    crossinline body: MethodHandler1<BodyT, ResponseT>,
): Route = defineGroup<ResourceT>(describer) { method(method, body) }
