package me.nekoalice.mafia.api.server.validation

private const val NO_MESSAGE_PROVIDED = "No message provided"

open class ValidationException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

fun <T> T.runValidation(validator: T.() -> Unit): Result<Unit> {
    return try {
        Result.success(validator())
    } catch (e: ValidationException) {
        Result.failure(e)
    } catch (e: IllegalStateException) {
        Result.failure(ValidationException(e.message ?: NO_MESSAGE_PROVIDED, e))
    } catch (e: IllegalArgumentException) {
        Result.failure(ValidationException(e.message ?: NO_MESSAGE_PROVIDED, e))
    }
}
