package group.phorus.exception.handling.handlers

import group.phorus.exception.handling.BaseException
import group.phorus.exception.handling.config.MetricsRecorder
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
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

/**
 * Controller exception handler that catches exceptions thrown inside `@RestController` methods
 * and converts them to standardized [ApiError] JSON responses.
 *
 * This handler is registered as `@RestControllerAdvice` and catches exceptions from:
 * - Business logic ([BaseException] subclasses like `NotFound`, `BadRequest`, etc.)
 * - Bean validation failures (`@Valid`, `@Validated` annotations)
 * - Type conversion errors (invalid path variables, query params)
 * - Database constraint violations
 * - Spring framework exceptions
 *
 * All caught exceptions are logged at **debug level** (configurable via `application.yml`)
 * and optionally recorded as metrics (see [MetricsRecorder]).
 *
 * **Logging configuration:**
 * ```yaml
 * logging:
 *   level:
 *     group.phorus.exception.handling: DEBUG  # Shows all exception logs
 * ```
 *
 * **Note:** This handler does **not** catch exceptions thrown in WebFilters: those are
 * handled by [WebfluxExceptionHandler].
 *
 * @see WebfluxExceptionHandler for filter-level exception handling
 * @see MetricsRecorder for optional metrics recording
 */
@RestControllerAdvice
class RestExceptionHandler(
    metricsProvider: ObjectProvider<MetricsRecorder>,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val metrics = metricsProvider.getIfAvailable()

    @ExceptionHandler(WebExchangeBindException::class)
    protected fun handleMethodArgumentNotValid(ex: WebExchangeBindException): ResponseEntity<Any> {
        logger.debug("Validation error: {}", ex.message)
        metrics?.record(ex, HttpStatus.BAD_REQUEST)
        return ApiError(HttpStatus.BAD_REQUEST, "Validation error")
            .apply {
                addValidationErrors(ex.bindingResult.fieldErrors)
                addValidationError(ex.bindingResult.globalErrors)
            }
            .let { ResponseEntity(it, it.status) }
    }

    @ExceptionHandler(ConstraintViolationException::class)
    protected fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<Any> {
        logger.debug("Constraint violation: {}", ex.message)
        metrics?.record(ex, HttpStatus.BAD_REQUEST)
        return ApiError(HttpStatus.BAD_REQUEST, "Validation error")
            .apply { addValidationErrors(ex.constraintViolations) }
            .let { ResponseEntity(it, it.status) }
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    protected fun handleMethodArgumentTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<Any> {
        logger.debug("Method argument type mismatch: parameter={}, value={}, requiredType={}",
            ex.name, ex.value, ex.requiredType?.simpleName)
        metrics?.record(ex, HttpStatus.BAD_REQUEST)
        return ApiError(HttpStatus.BAD_REQUEST, "The parameter ${ex.name} of value ${ex.value} " +
                "could not be converted to type ${ex.requiredType?.simpleName}")
            .let { ResponseEntity(it, it.status) }
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    protected fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): ResponseEntity<Any> {
        logger.debug("Data integrity violation: {}", ex.message)
        metrics?.record(ex, HttpStatus.CONFLICT)
        return ApiError(HttpStatus.CONFLICT, "A conflict with a unique field was found")
            .let { ResponseEntity(it, it.status) }
    }

    @ExceptionHandler(ServerWebInputException::class)
    protected fun handleServerWebInput(ex: ServerWebInputException): ResponseEntity<Any> {
        logger.debug("Server web input error: {}", ex.reason)
        metrics?.record(ex, HttpStatus.BAD_REQUEST)
        return ApiError(HttpStatus.BAD_REQUEST, ex.reason ?: "Invalid input")
            .let { ResponseEntity(it, it.status) }
    }

    @ExceptionHandler(ResponseStatusException::class)
    protected fun handleResponseStatus(ex: ResponseStatusException): ResponseEntity<Any> {
        val httpStatus = HttpStatus.valueOf(ex.statusCode.value())
        logger.debug("Response status exception: status={}, reason={}", httpStatus, ex.reason)
        metrics?.record(ex, httpStatus)
        return ApiError(httpStatus, ex.reason ?: httpStatus.toString())
            .let { ResponseEntity(it, it.status) }
    }

    @ExceptionHandler(TimeoutException::class)
    protected fun handleTimeoutExceptions(ex: TimeoutException): ResponseEntity<Any> {
        logger.debug("Timeout exception: {}", ex.message)
        metrics?.record(ex, HttpStatus.REQUEST_TIMEOUT)
        return ApiError(HttpStatus.REQUEST_TIMEOUT, ex.message ?: "Request timeout")
            .let { ResponseEntity(it, it.status) }
    }

    @ExceptionHandler(BaseException::class)
    protected fun handleBaseExceptions(ex: BaseException): ResponseEntity<Any> {
        logger.debug("{}: {}", ex.javaClass.simpleName, ex.message)
        metrics?.record(ex, ex.httpStatus)
        return ApiError(ex.httpStatus, ex.message ?: ex.httpStatus.name)
            .let { ResponseEntity(it, it.status) }
    }

    @ExceptionHandler(Exception::class)
    protected fun handleOtherExceptions(ex: Exception): ResponseEntity<Any> {
        logger.error("Unhandled exception: ${ex.javaClass.simpleName} - ${ex.message}", ex)
        metrics?.record(ex, HttpStatus.INTERNAL_SERVER_ERROR)
        return ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error")
            .let { ResponseEntity(it, it.status) }
    }
}
