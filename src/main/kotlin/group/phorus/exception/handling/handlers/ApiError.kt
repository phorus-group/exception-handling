package group.phorus.exception.handling.handlers

import com.fasterxml.jackson.annotation.JsonFormat
import org.hibernate.validator.internal.engine.path.PathImpl
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import java.time.LocalDateTime
import jakarta.validation.ConstraintViolation

data class ApiError(
    val status: HttpStatus,
    val message: String? = null,
    val debugMessage: String? = null,
) {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    val timestamp = LocalDateTime.now()

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

    fun addValidationErrors(fieldErrors: List<FieldError>) =
        fieldErrors.forEach {
            addValidationError(
                obj = it.objectName,
                message = it.defaultMessage,
                field = it.field,
                rejectedValue = it.rejectedValue,
            )
        }

    fun addValidationError(globalErrors: List<ObjectError>) =
        globalErrors.forEach {
            addValidationError(
                obj = it.objectName,
                message = it.defaultMessage,
            )
        }

    /**
     * Utility method for adding error of ConstraintViolation. Usually used when a @Validated validation fails.
     * @param constraintViolations the ConstraintViolations
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
