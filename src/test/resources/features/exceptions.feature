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

  Scenario: Caller with an object that will result in a RequestTimeout exception
    Given the caller has an object that will result in a RequestTimeout exception
    When the external service calls the "/v1/testException" endpoint
    Then the service returns HTTP 408
    And the service returns a message with the error REQUEST_TIMEOUT

  Scenario: Caller with an object that will result in a InternalServerError exception
    Given the caller has an object that will result in a InternalServerError exception
    When the external service calls the "/v1/testException" endpoint
    Then the service returns HTTP 500
    And the service returns a message with the error INTERNAL_SERVER_ERROR

  Scenario: Caller with an object that will result in a MethodNotAllowed exception
    Given the caller has an object that will result in a MethodNotAllowed exception
    When the external service calls the "/v1/testException" endpoint
    Then the service returns HTTP 405
    And the service returns a message with the error METHOD_NOT_ALLOWED

  Scenario: Caller with an object that will result in a TooManyRequests exception
    Given the caller has an object that will result in a TooManyRequests exception
    When the external service calls the "/v1/testException" endpoint
    Then the service returns HTTP 429
    And the service returns a message with the error TOO_MANY_REQUESTS

  Scenario: Caller with an object that will result in a ServiceUnavailable exception
    Given the caller has an object that will result in a ServiceUnavailable exception
    When the external service calls the "/v1/testException" endpoint
    Then the service returns HTTP 503
    And the service returns a message with the error SERVICE_UNAVAILABLE

  Scenario: Caller with an object that will result in a BadGateway exception
    Given the caller has an object that will result in a BadGateway exception
    When the external service calls the "/v1/testException" endpoint
    Then the service returns HTTP 502
    And the service returns a message with the error BAD_GATEWAY

  Scenario: Caller with an object that will result in a GatewayTimeout exception
    Given the caller has an object that will result in a GatewayTimeout exception
    When the external service calls the "/v1/testException" endpoint
    Then the service returns HTTP 504
    And the service returns a message with the error GATEWAY_TIMEOUT

  Scenario: Caller with an object that will result in a UnprocessableEntity exception
    Given the caller has an object that will result in a UnprocessableEntity exception
    When the external service calls the "/v1/testException" endpoint
    Then the service returns HTTP 422
    And the service returns a message with the error UNPROCESSABLE_CONTENT

  Scenario: Caller with an object that will result in a Gone exception
    Given the caller has an object that will result in a Gone exception
    When the external service calls the "/v1/testException" endpoint
    Then the service returns HTTP 410
    And the service returns a message with the error GONE

  Scenario: Caller with an object that will result in a PreconditionFailed exception
    Given the caller has an object that will result in a PreconditionFailed exception
    When the external service calls the "/v1/testException" endpoint
    Then the service returns HTTP 412
    And the service returns a message with the error PRECONDITION_FAILED

  Scenario: Caller with an object that will result in a UnsupportedMediaType exception
    Given the caller has an object that will result in a UnsupportedMediaType exception
    When the external service calls the "/v1/testException" endpoint
    Then the service returns HTTP 415
    And the service returns a message with the error UNSUPPORTED_MEDIA_TYPE

  Scenario: Caller with an object that will result in a unexpected exception
    When the external service calls the "/v1/testFail" endpoint
    Then the service returns HTTP 500
    And the service returns a message with the error INTERNAL_SERVER_ERROR
