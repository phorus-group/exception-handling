package group.phorus.exception.handling.handlers

data class ValidationError(
    val obj: String,
    val field: String? = null,
    val rejectedValue: Any? = null,
    val message: String? = null,
)