package group.phorus.exception.handling.handlers

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.validator.internal.engine.path.PathImpl
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import java.time.LocalDateTime
import jakarta.validation.ConstraintViolation

/**
 * Standardized error response returned by [RestExceptionHandler] and [WebfluxExceptionHandler].
 *
 * Every exception caught by the handlers is converted to this JSON structure before
 * being sent to the client. This ensures all errors have the same shape regardless of
 * where they originated.
 *
 * Example JSON response:
 * ```json
 * {
 *   "timestamp": "06-03-2026 10:30:00",
 *   "status": "NOT_FOUND",
 *   "message": "User with id 550e8400-e29b-41d4-a716-446655440000 not found"
 * }
 * ```
 *
 * For validation errors, the response includes a `validationErrors` array:
 * ```json
 * {
 *   "timestamp": "06-03-2026 10:30:00",
 *   "status": "BAD_REQUEST",
 *   "message": "Validation error",
 *   "validationErrors": [
 *     {
 *       "obj": "createUserRequest",
 *       "field": "email",
 *       "rejectedValue": null,
 *       "message": "Cannot be blank"
 *     }
 *   ]
 * }
 * ```
 *
 * @property status The HTTP status (e.g., `HttpStatus.NOT_FOUND`). Hidden from JSON, only `statusName` is serialized.
 * @property message Human-readable error message
 * @property debugMessage Optional debug information
 */
data class ApiError(
    @get:JsonIgnore val status: HttpStatus,
    val message: String? = null,
    val debugMessage: String? = null,
) {

    /** Timestamp when the error occurred, formatted as "dd-MM-yyyy hh:mm:ss" */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    val timestamp = LocalDateTime.now()

    /** HTTP status as a string (e.g., "NOT_FOUND", "BAD_REQUEST"). Serialized as "status" in JSON. */
    @get:JsonProperty("status")
    val statusName: String get() = status.name

    /** List of validation errors. Only present when validation fails (e.g., `@Valid` annotation). */
    var validationErrors: MutableList<ValidationError>? = null

    private fun addValidationError(obj: String, message: String?, field: String? = null, rejectedValue: Any? = null) {
        if (validationErrors == null) validationErrors = mutableListOf()
        validationErrors!!.add(ValidationError(
            obj = obj,
            field = field,
            rejectedValue = rejectedValue,
            message = message,
        ))
    }

    /**
     * Adds field-level validation errors from Spring's `FieldError` objects.
     *
     * Called by [RestExceptionHandler] when handling `WebExchangeBindException`.
     */
    fun addValidationErrors(fieldErrors: List<FieldError>) =
        fieldErrors.forEach {
            addValidationError(
                obj = it.objectName,
                message = it.defaultMessage,
                field = it.field,
                rejectedValue = it.rejectedValue,
            )
        }

    /**
     * Adds global (non-field) validation errors from Spring's `ObjectError` objects.
     *
     * Called by [RestExceptionHandler] when handling `WebExchangeBindException`.
     */
    fun addValidationError(globalErrors: List<ObjectError>) =
        globalErrors.forEach {
            addValidationError(
                obj = it.objectName,
                message = it.defaultMessage,
            )
        }

    /**
     * Adds constraint violations from Jakarta Bean Validation.
     *
     * Called by [RestExceptionHandler] when handling `ConstraintViolationException`,
     * which occurs when `@Validated` is used on controllers for method-level validation.
     */
    fun addValidationErrors(constraintViolations: Set<ConstraintViolation<*>>) =
        constraintViolations.forEach {
            addValidationError(
                obj = it.rootBeanClass.simpleName,
                message = it.message,
                field = (it.propertyPath as PathImpl).leafNode.asString(),
                rejectedValue = it.invalidValue,
            )
        }
}
