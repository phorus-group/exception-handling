package group.phorus.exception.handling.bdd

import group.phorus.exception.handling.*
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty

@RestController
@Validated
class TestController {

    @PostMapping(path = ["/v1/test"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    suspend fun test(
        @RequestBody
        @Valid
        request: TestObject,
    ): TestObject = request

    @PostMapping(path = ["/v1/testList"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    suspend fun testList(
        @RequestBody
        @Valid
        @NotEmpty(message = "Cannot be empty")
        requests: List<TestObject>,
    ): List<TestObject> = requests

    @PostMapping(path = ["/v1/testFail"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    suspend fun testFail(
        @RequestBody
        requests: BaseException, // Cannot be built by jackson, so it'll throw an unhandled exception
    ): BaseException = requests

    @PostMapping(path = ["/v1/testException"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    suspend fun testException(
        @RequestBody
        @Valid
        request: String,
    ): TestObject = when (request) {
        "BadRequest" -> throw BadRequest("Bad request")
        "Conflict" -> throw Conflict("Conflict")
        "NotFound" -> throw NotFound("Not found")
        "Unauthorized" -> throw Unauthorized("Unauthorized")
        "Forbidden" -> throw Forbidden("Forbidden")
        else -> throw Exception("The exception doesn't exist")
    }
}