Feature: Exceptions are handled and a custom message is sent to the client
  If any exception occurs, it should be handled and a custom message
  with the right HTTP code should be sent back to the client.

  Scenario: Caller with an object that will result in a BadRequest exception
    Given the caller has an object that will result in a BadRequest exception
    When the external service calls the "/v1/testException" endpoint
    Then the service returns HTTP 400
    And the service returns a message with the error BAD_REQUEST

  Scenario: Caller with an object that will result in a NotFound exception
    Given the caller has an object that will result in a NotFound exception
    When the external service calls the "/v1/testException" endpoint
    Then the service returns HTTP 404
    And the service returns a message with the error NOT_FOUND

  Scenario: Caller with an object that will result in a Conflict exception
    Given the caller has an object that will result in a Conflict exception
    When the external service calls the "/v1/testException" endpoint
    Then the service returns HTTP 409
    And the service returns a message with the error CONFLICT

  Scenario: Caller with an object that will result in a Unauthorized exception
    Given the caller has an object that will result in a Unauthorized exception
    When the external service calls the "/v1/testException" endpoint
    Then the service returns HTTP 401
    And the service returns a message with the error UNAUTHORIZED

  Scenario: Caller with an object that will result in a Forbidden exception
    Given the caller has an object that will result in a Forbidden exception
    When the external service calls the "/v1/testException" endpoint
    Then the service returns HTTP 403
    And the service returns a message with the error FORBIDDEN

  Scenario: Caller with an object that will result in a unexpected exception
    When the external service calls the "/v1/testFail" endpoint
    Then the service returns HTTP 500
    And the service returns a message with the error INTERNAL_SERVER_ERROR