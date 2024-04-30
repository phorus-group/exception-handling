package group.phorus.userservice.exceptions

import org.springframework.http.HttpStatus

sealed class BaseException(message: String?, val httpStatus: HttpStatus) : RuntimeException(message)

class BadRequest(message: String?) : BaseException(message, HttpStatus.BAD_REQUEST)
class Conflict(message: String?) : BaseException(message, HttpStatus.CONFLICT)
class NotFound(message: String?) : BaseException(message, HttpStatus.NOT_FOUND)
class Unauthorized(message: String?) : BaseException(message, HttpStatus.UNAUTHORIZED)
class Forbidden(message: String?) : BaseException(message, HttpStatus.FORBIDDEN)
