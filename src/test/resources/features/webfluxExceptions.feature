Feature: Webflux exceptions are handled and a custom message is sent to the client
  Webflux exceptions, such as any exception thrown in WebFilters, should be
  handled and a custom message with the right HTTP code should be sent
  back to the client.

  Scenario: Caller with a normal object is filtered out by a web filter
    Given the caller has a normal object
    When the external service calls the "/v1/failFilter" endpoint
    Then the service returns HTTP 401
    And the service returns a message with the error UNAUTHORIZED
