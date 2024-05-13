package group.phorus.exception.handling.bdd

import group.phorus.exception.handling.BaseException
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class TestException(
    val exception: BaseException? = null,
)

data class TestObject(
    @field:NotBlank(message = "Cannot be blank")
    val testVar: String? = null,

    @field:NotNull(message = "Cannot be null")
    val testInt: Int? = null,

    @field:Valid
    @field:NotNull(message = "Cannot be null")
    val subObject: TestSubObject? = null,

    @field:Valid
    @field:NotEmpty(message = "Cannot be empty")
    @field:NotNull(message = "Cannot be null")
    val subObjectList: List<TestSubObject>? = null,
)

data class TestSubObject(
    @field:NotBlank(message = "Cannot be blank")
    val testVar: String,
)

