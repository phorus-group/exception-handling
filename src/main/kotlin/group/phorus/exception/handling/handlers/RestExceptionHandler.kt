package group.phorus.exception.handling.handlers

import group.phorus.exception.handling.BaseException
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebInputException
import java.util.concurrent.TimeoutException


@RestControllerAdvice
class RestExceptionHandler {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(WebExchangeBindException::class)
    protected fun handleMethodArgumentNotValid(ex: WebExchangeBindException): ResponseEntity<Any> =
        ApiError(HttpStatus.BAD_REQUEST, "Validation error")
            .apply {
                addValidationErrors(ex.bindingResult.fieldErrors)
                addValidationError(ex.bindingResult.globalErrors)
            }
            .let { ResponseEntity(it, it.status) }

    @ExceptionHandler(ConstraintViolationException::class)
    protected fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<Any> =
        ApiError(HttpStatus.BAD_REQUEST, "Validation error")
            .apply { addValidationErrors(ex.constraintViolations) }
            .let { ResponseEntity(it, it.status) }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    protected fun handleMethodArgumentTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<Any> =
        ApiError(HttpStatus.BAD_REQUEST, "The parameter ${ex.name} of value ${ex.value} " +
                "could not be converted to type ${ex.requiredType?.simpleName}")
            .let { ResponseEntity(it, it.status) }

    // Database constraint violations
    @ExceptionHandler(DataIntegrityViolationException::class)
    protected fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): ResponseEntity<Any> =
        ApiError(HttpStatus.CONFLICT, "A conflict with a unique field was found")
            .let { ResponseEntity(it, it.status) }

    // Server web input exceptions
    @ExceptionHandler(ServerWebInputException::class)
    protected fun handleServerWebInput(ex: ServerWebInputException): ResponseEntity<Any> =
        ApiError(HttpStatus.BAD_REQUEST, ex.reason ?: "Invalid input")
            .let { ResponseEntity(it, it.status) }

    // Response status exceptions
    @ExceptionHandler(ResponseStatusException::class)
    protected fun handleResponseStatus(ex: ResponseStatusException): ResponseEntity<Any> =
        ApiError(ex.statusCode as HttpStatus, ex.reason ?: ex.statusCode.toString())
            .let { ResponseEntity(it, it.status) }

    @ExceptionHandler(TimeoutException::class)
    protected fun handleTimeoutExceptions(ex: TimeoutException): ResponseEntity<Any> =
        ApiError(HttpStatus.REQUEST_TIMEOUT, ex.message ?: "Request timeout")
            .let { ResponseEntity(it, it.status) }

    @ExceptionHandler(BaseException::class)
    protected fun handleBaseExceptions(ex: BaseException): ResponseEntity<Any> =
        ApiError(ex.httpStatus, ex.message ?: ex.httpStatus.name)
            .let { ResponseEntity(it, it.status) }

    @ExceptionHandler(Exception::class)
    protected fun handleOtherExceptions(ex: Exception): ResponseEntity<Any> {
        logger.error("Unhandled exception: ${ex.javaClass.simpleName} - ${ex.message}", ex)
        return ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error")
            .let { ResponseEntity(it, it.status) }
    }
}
