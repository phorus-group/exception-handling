package group.phorus.exception.handling.bdd.steps

import group.phorus.exception.handling.bdd.RequestScenarioScope
import group.phorus.exception.handling.bdd.ResponseScenarioScope
import group.phorus.exception.handling.bdd.TestObject
import group.phorus.exception.handling.bdd.TestSubObject
import group.phorus.exception.handling.BadRequest
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient


class BaseStepsDefinition(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val requestScenarioScope: RequestScenarioScope,
    @Autowired private val responseScenarioScope: ResponseScenarioScope,
) {

    @Given("^the caller has an object that will result in a (BadRequest|NotFound|Conflict|Unauthorized|Forbidden) exception$")
    fun `the caller has an object that will result in a {exception} exception`(exception: String) {
        requestScenarioScope.request = exception
    }

    @Given("the caller has a normal object")
    fun `the caller has a normal object`() {
        requestScenarioScope.request = TestObject(
            testVar = "test",
            testInt = 1,
            subObject = TestSubObject(testVar = "test1"),
            subObjectList = listOf(TestSubObject(testVar = "test2")),
        )
    }

    @Given("the caller has a list containing an object with null field that cannot be null")
    fun `the caller has a list containing an object with null field that cannot be null`() {
        requestScenarioScope.request = listOf(
            TestObject(
                testVar = "test",
                testInt = null,
                subObject = TestSubObject(testVar = "test1"),
                subObjectList = listOf(TestSubObject(testVar = "test2")),
            )
        )
    }

    @Given("the caller has a null field that cannot be null")
    fun `the caller has a null field that cannot be null`() {
        requestScenarioScope.request = TestObject(
            testVar = "test",
            testInt = null,
            subObject = TestSubObject(testVar = "test1"),
            subObjectList = listOf(TestSubObject(testVar = "test2")),
        )
    }

    @Given("the caller has a blank field that cannot be blank")
    fun `the caller has a blank field that cannot be blank`() {
        requestScenarioScope.request = TestObject(
            testVar = "",
            testInt = 1,
            subObject = TestSubObject(testVar = "test1"),
            subObjectList = listOf(TestSubObject(testVar = "test2")),
        )
    }

    @Given("the caller has a null field that cannot be blank")
    fun `the caller has a null field that cannot be blank`() {
        requestScenarioScope.request = TestObject(
            testVar = null,
            testInt = 1,
            subObject = TestSubObject(testVar = "test1"),
            subObjectList = listOf(TestSubObject(testVar = "test2")),
        )
    }

    @Given("the caller has an empty list field that cannot be empty")
    fun `the caller has an empty list field that cannot be empty`() {
        requestScenarioScope.request = TestObject(
            testVar = "test",
            testInt = 1,
            subObject = TestSubObject(testVar = "test1"),
            subObjectList = emptyList(),
        )
    }

    @Given("the caller has a null list field that cannot be null")
    fun `the caller has a null list field that cannot be null`() {
        requestScenarioScope.request = TestObject(
            testVar = "test",
            testInt = 1,
            subObject = TestSubObject(testVar = "test1"),
            subObjectList = null,
        )
    }

    @Given("the caller has a blank subfield that cannot be blank")
    fun `the caller has a blank subfield that cannot be blank`() {
        requestScenarioScope.request = TestObject(
            testVar = "test",
            testInt = 1,
            subObject = TestSubObject(testVar = ""),
            subObjectList = listOf(TestSubObject(testVar = "test2")),
        )
    }


    @When("the external service calls the {string} endpoint")
    fun `when the external service calls the {string} endpoint`(endpoint: String) {
        webTestClient.post()
            .uri { it.path(endpoint).build() }
            .bodyValue(requestScenarioScope.request!!)
            .exchange()
            .let { responseScenarioScope.responseSpec = it }
    }


    @Then("the service returns HTTP {int}")
    fun `and the service returns HTTP code`(httpCode: Int) {
        responseScenarioScope.responseSpec!!
            .expectStatus().isEqualTo(httpCode)
    }

    @Then("the service returns a message with the validation errors")
    fun `the service returns a message with the validation errors`(data: DataTable) {
        val obj = data.asMaps().first()["obj"]!!
        val field = data.asMaps().first()["field"]!!
        val rejectedValue = data.asMaps().first()["rejectedValue"]!!
        val message = data.asMaps().first()["message"]!!

        responseScenarioScope.responseSpec!!
            .expectBody()
            .jsonPath("$.validationErrors[0].obj").isEqualTo(obj)
            .jsonPath("$.validationErrors[0].field").isEqualTo(field)
            .let {
                when (rejectedValue) {
                    "null" -> it.jsonPath("$.validationErrors[0].rejectedValue").doesNotExist()
                    "blank" -> it.jsonPath("$.validationErrors[0].rejectedValue").isEqualTo("")
                    "[]" -> it.jsonPath("$.validationErrors[0].rejectedValue").isEmpty
                    else -> it.jsonPath("$.validationErrors[0].rejectedValue").isEqualTo(rejectedValue)
                }
            }
            .jsonPath("$.validationErrors[0].message").isEqualTo(message)
    }

    @Then("^the service returns a message with the error (BAD_REQUEST|NOT_FOUND|CONFLICT|UNAUTHORIZED|FORBIDDEN|INTERNAL_SERVER_ERROR)$")
    fun `the service returns a message with the error {string}`(error: String) {
        responseScenarioScope.responseSpec!!
            .expectBody()
            .jsonPath("$.status").isEqualTo(error)
    }
}