package group.phorus.exception.handling

import org.springframework.http.HttpStatus

/**
 * Base class for all HTTP exceptions in the exception-handling library.
 *
 * Extending this sealed class allows creating custom exceptions that are automatically
 * caught by [RestExceptionHandler] and [WebfluxExceptionHandler] and converted to
 * structured JSON error responses with the correct HTTP status code.
 *
 * @param message Human-readable error message returned to the client
 * @param httpStatus HTTP status code to return (e.g., `HttpStatus.NOT_FOUND`)
 */
sealed class BaseException(message: String?, val httpStatus: HttpStatus) : RuntimeException(message)

/** Returns HTTP 400 BAD_REQUEST */
class BadRequest(message: String?) : BaseException(message, HttpStatus.BAD_REQUEST)

/** Returns HTTP 409 CONFLICT */
class Conflict(message: String?) : BaseException(message, HttpStatus.CONFLICT)

/** Returns HTTP 404 NOT_FOUND */
class NotFound(message: String?) : BaseException(message, HttpStatus.NOT_FOUND)

/** Returns HTTP 401 UNAUTHORIZED */
class Unauthorized(message: String?) : BaseException(message, HttpStatus.UNAUTHORIZED)

/** Returns HTTP 403 FORBIDDEN */
class Forbidden(message: String?) : BaseException(message, HttpStatus.FORBIDDEN)

/** Returns HTTP 408 REQUEST_TIMEOUT */
class RequestTimeout(message: String?) : BaseException(message, HttpStatus.REQUEST_TIMEOUT)

/** Returns HTTP 500 INTERNAL_SERVER_ERROR */
class InternalServerError(message: String?) : BaseException(message, HttpStatus.INTERNAL_SERVER_ERROR)

/** Returns HTTP 405 METHOD_NOT_ALLOWED */
class MethodNotAllowed(message: String?) : BaseException(message, HttpStatus.METHOD_NOT_ALLOWED)

/** Returns HTTP 429 TOO_MANY_REQUESTS */
class TooManyRequests(message: String?) : BaseException(message, HttpStatus.TOO_MANY_REQUESTS)

/** Returns HTTP 503 SERVICE_UNAVAILABLE */
class ServiceUnavailable(message: String?) : BaseException(message, HttpStatus.SERVICE_UNAVAILABLE)

/** Returns HTTP 502 BAD_GATEWAY */
class BadGateway(message: String?) : BaseException(message, HttpStatus.BAD_GATEWAY)

/** Returns HTTP 504 GATEWAY_TIMEOUT */
class GatewayTimeout(message: String?) : BaseException(message, HttpStatus.GATEWAY_TIMEOUT)

/** Returns HTTP 422 UNPROCESSABLE_CONTENT (formerly UNPROCESSABLE_ENTITY per RFC 9110) */
class UnprocessableEntity(message: String?) : BaseException(message, HttpStatus.UNPROCESSABLE_CONTENT)

/** Returns HTTP 410 GONE */
class Gone(message: String?) : BaseException(message, HttpStatus.GONE)

/** Returns HTTP 412 PRECONDITION_FAILED */
class PreconditionFailed(message: String?) : BaseException(message, HttpStatus.PRECONDITION_FAILED)

/** Returns HTTP 415 UNSUPPORTED_MEDIA_TYPE */
class UnsupportedMediaType(message: String?) : BaseException(message, HttpStatus.UNSUPPORTED_MEDIA_TYPE)
