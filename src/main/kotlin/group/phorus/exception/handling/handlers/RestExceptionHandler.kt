package group.phorus.exception.handling.handlers

import group.phorus.exception.handling.BaseException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.util.concurrent.TimeoutException
import jakarta.validation.ConstraintViolationException


@RestControllerAdvice
class RestExceptionHandler {

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

    @ExceptionHandler(TimeoutException::class)
    protected fun handleTimeoutExceptions(ex: TimeoutException): ResponseEntity<Any> =
        ApiError(HttpStatus.REQUEST_TIMEOUT, ex.message)
            .let { ResponseEntity(it, it.status) }


    @ExceptionHandler(BaseException::class)
    protected fun handleBaseExceptions(ex: BaseException): ResponseEntity<Any> =
        ApiError(ex.httpStatus, ex.message ?: ex.httpStatus.name)
            .let { ResponseEntity(it, it.status) }

    @ExceptionHandler(Exception::class)
    protected fun handleOtherExceptions(ex: Exception): ResponseEntity<Any> {
        val apiError = if (ex.message?.lowercase()?.contains("unique") == true) {
            ApiError(HttpStatus.CONFLICT, "A conflict with a unique field was found")
        } else if (ex.message?.lowercase()?.contains("index|unique|constraint|violation".toRegex()) == true) {
            ApiError(HttpStatus.BAD_REQUEST, "Validation error")
        } else if (ex.message?.lowercase()?.contains("no property .* found for type".toRegex()) == true) {
            ApiError(HttpStatus.BAD_REQUEST, ex.message)
        } else ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.message ?: HttpStatus.INTERNAL_SERVER_ERROR.name)

        return ResponseEntity(apiError, apiError.status)
    }
}
