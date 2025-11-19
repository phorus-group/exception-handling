package group.phorus.exception.handling

import org.springframework.http.HttpStatus

sealed class BaseException(message: String?, val httpStatus: HttpStatus) : RuntimeException(message)

class BadRequest(message: String?) : BaseException(message, HttpStatus.BAD_REQUEST)
class Conflict(message: String?) : BaseException(message, HttpStatus.CONFLICT)
class NotFound(message: String?) : BaseException(message, HttpStatus.NOT_FOUND)
class Unauthorized(message: String?) : BaseException(message, HttpStatus.UNAUTHORIZED)
class Forbidden(message: String?) : BaseException(message, HttpStatus.FORBIDDEN)
class RequestTimeout(message: String?) : BaseException(message, HttpStatus.REQUEST_TIMEOUT)
class InternalServerError(message: String?) : BaseException(message, HttpStatus.INTERNAL_SERVER_ERROR)
class MethodNotAllowed(message: String?) : BaseException(message, HttpStatus.METHOD_NOT_ALLOWED)
class TooManyRequests(message: String?) : BaseException(message, HttpStatus.TOO_MANY_REQUESTS)
class ServiceUnavailable(message: String?) : BaseException(message, HttpStatus.SERVICE_UNAVAILABLE)
class BadGateway(message: String?) : BaseException(message, HttpStatus.BAD_GATEWAY)
class GatewayTimeout(message: String?) : BaseException(message, HttpStatus.GATEWAY_TIMEOUT)
class UnprocessableEntity(message: String?) : BaseException(message, HttpStatus.UNPROCESSABLE_ENTITY)
class Gone(message: String?) : BaseException(message, HttpStatus.GONE)
class PreconditionFailed(message: String?) : BaseException(message, HttpStatus.PRECONDITION_FAILED)
class UnsupportedMediaType(message: String?) : BaseException(message, HttpStatus.UNSUPPORTED_MEDIA_TYPE)
