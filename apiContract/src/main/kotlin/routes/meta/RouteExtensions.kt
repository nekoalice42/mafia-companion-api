package me.nekoalice.mafia.api.contracts.routes.meta

import io.ktor.http.*
import io.ktor.server.resources.*
import io.ktor.server.resources.patch
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import me.nekoalice.mafia.api.contracts.BaseAPI
import me.nekoalice.mafia.api.contracts.openapi.OpenAPIRouteDescriber

internal typealias KtorMethodBody1<T> = suspend RoutingContext.(T) -> Unit
internal typealias KtorMethodBody2<R, T> = suspend RoutingContext.(R, T) -> Unit
internal typealias KtorRegisterFunction1<T> = Route.(KtorMethodBody1<T>) -> Route
internal typealias KtorRegisterFunction2<R, T> = Route.(KtorMethodBody2<R, T>) -> Route
internal typealias MethodHandler<R, RT> = suspend RoutingContext.(R) -> BaseAPI.Response<RT>
internal typealias MethodHandlerWithBody<R, B, RT> =
        suspend RoutingContext.(R, B) -> BaseAPI.Response<RT>

@PublishedApi
@JvmName("getRegisterFunction1")
internal inline fun <reified R : Any> getRegisterFunction(
    method: HttpMethod,
): KtorRegisterFunction1<R> = when (method) {
    Get -> Route::get
    Post -> Route::post
    Put -> Route::put
    Patch -> Route::patch
    Delete -> Route::delete
    Head -> Route::head
    Options -> Route::options
    else -> throw IllegalArgumentException("Unsupported method: $method")
}

@PublishedApi
@JvmName("getRegisterFunction2")
internal inline fun <reified R : Any, reified T> getRegisterFunction(
    method: HttpMethod,
): KtorRegisterFunction2<R, T> = when (method) {
    Post -> Route::post
    Put -> Route::put
    Patch -> Route::patch

    Get,
    Delete,
    Head,
    Options,
        -> throw IllegalArgumentException("Method cannot have body: $method")

    else -> throw IllegalArgumentException("Unsupported method: $method")
}

internal fun OpenAPIRouteDescriber?.assertMethodSupported(method: HttpMethod) {
    if (this == null) return
    require(method in supportedMethods) { "Method $method is not supported by describer" }
}

@ExperimentalKtorApi
internal fun describeOrHide(route: Route, describer: OpenAPIRouteDescriber?, method: HttpMethod) {
    if (describer != null) {
        route.describe { describer.describe(method, this) }
    } else {
        route.hide()
    }
}

@IgnorableReturnValue
@OptIn(ExperimentalKtorApi::class)
@JvmName("routeDefineResource")
internal inline fun <reified ResourceT : Any, reified ResponseT : Any> Route.define(
    method: HttpMethod,
    describer: OpenAPIRouteDescriber?,
    crossinline body: MethodHandler<ResourceT, ResponseT>,
): Route {
    describer.assertMethodSupported(method)
    val register = getRegisterFunction<ResourceT>(method)

    return register { resource ->
        body(resource).sendInResponseTo(call)
    }.also { describeOrHide(it, describer, method) }
}

@IgnorableReturnValue
@OptIn(ExperimentalKtorApi::class)
@JvmName("routeDefineBody")
internal inline fun <reified ResourceT : Any, reified BodyT, reified ResponseT : Any> Route.define(
    method: HttpMethod,
    describer: OpenAPIRouteDescriber?,
    crossinline body: MethodHandler<BodyT, ResponseT>,
): Route {
    describer.assertMethodSupported(method)
    val register = getRegisterFunction<ResourceT, BodyT>(method)

    return register { _, requestBody ->
        body(requestBody).sendInResponseTo(call)
    }.also { describeOrHide(it, describer, method) }
}

@IgnorableReturnValue
@OptIn(ExperimentalKtorApi::class)
@JvmName("routeDefineResourceBody")
internal inline fun <reified ResourceT : Any, reified BodyT, reified ResponseT : Any> Route.define(
    method: HttpMethod,
    describer: OpenAPIRouteDescriber?,
    crossinline body: MethodHandlerWithBody<ResourceT, BodyT, ResponseT>,
): Route {
    describer.assertMethodSupported(method)
    val register = getRegisterFunction<ResourceT, BodyT>(method)

    return register { resource, requestBody ->
        body(resource, requestBody).sendInResponseTo(call)
    }.also { describeOrHide(it, describer, method) }
}
