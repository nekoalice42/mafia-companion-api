package me.nekoalice.mafia.api.contracts.openapi.descriptions

import io.ktor.http.*
import io.ktor.openapi.*
import me.nekoalice.mafia.api.dto.response.ErrorResponse

@Deprecated(
    "Use explicit `successResponse`s",
    ReplaceWith($$"successResponse(\"$what created\", Created); successResponse(\"$what updated\", NoContent)"),
    DeprecationLevel.ERROR,
)
internal fun Responses.Builder.commonPutResponses(what: String) {
    successResponse("$what created", Created)
    successResponse("$what updated", NoContent)
}

@Deprecated(
    "Use explicit `successResponse`s",
    ReplaceWith($$"successResponse(\"$what deleted or didn't exist before\", NoContent)"),
    DeprecationLevel.ERROR,
)
internal fun Responses.Builder.commonDeleteResponses(what: String) {
    successResponse("$what deleted or didn't exist before", NoContent)
}

internal inline fun <reified T> Responses.Builder.defaultKtorBodyErrorResponsesFor() {
    response(BadRequest, "Request body could not be converted to ${T::class.simpleName}")
    response(UnsupportedMediaType, "Request body content type is unsupported")
}

internal inline fun Responses.Builder.response(
    status: HttpStatusCode,
    description: String,
    crossinline extra: Response.Builder.() -> Unit = {},
) {
    status {
        this.description = description
        extra()
    }
}

internal inline fun <reified T : Any> Responses.Builder.responseOf(
    status: HttpStatusCode,
    description: String,
    crossinline extra: Response.Builder.() -> Unit = {},
) = response(status, description) {
    content {
        schema = jsonSchema<T>()
    }
    extra()
}

internal inline fun Responses.Builder.successResponse(
    description: String,
    status: HttpStatusCode = NoContent,
    crossinline extra: Response.Builder.() -> Unit = {},
) = response(status, description, extra)

internal inline fun <reified T : Any> Responses.Builder.successResponseOf(
    description: String,
    status: HttpStatusCode = OK,
    crossinline extra: Response.Builder.() -> Unit = {},
) = responseOf<T>(status, description, extra)

internal inline fun Responses.Builder.errorResponse(
    status: HttpStatusCode,
    description: String,
    crossinline extra: Response.Builder.() -> Unit = {},
) = responseOf<ErrorResponse>(status, description, extra)

internal inline fun <reified T : Any> Operation.Builder.requestBodyOf(
    description: String,
    crossinline extra: RequestBody.Builder.() -> Unit = {},
) {
    requestBody {
        required = true
        content {
            schema = jsonSchema<T>()
        }
        this.description = description
        extra()
    }
}
