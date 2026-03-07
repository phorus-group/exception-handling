# Exception Handling

[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/group.phorus/exception-handling)](https://mvnrepository.com/artifact/group.phorus/exception-handling)

Exception handling library for Spring Boot WebFlux services. Provides a sealed hierarchy of HTTP
exception classes, automatic error response formatting, bean validation support for Kotlin
coroutines, and optional OpenAPI schema registration. Add the dependency and every error your
service throws, whether from a controller, a WebFilter, or a validation annotation, is
automatically caught and returned to the client as a consistent JSON response.

### Notes

> The project runs a vulnerability analysis pipeline regularly,
> any found vulnerabilities will be fixed as soon as possible.

> The project dependencies are being regularly updated by [Renovate](https://github.com/phorus-group/renovate).
> Dependency updates that don't break tests will be automatically deployed with an updated patch version.

> The project has been thoroughly tested to ensure that it is safe to use in a production environment.

## Table of contents

- [Background: why standardized error responses?](#background-why-standardized-error-responses)
- [Features](#features)
- [Getting started](#getting-started)
  - [Installation](#installation)
  - [Quick start](#quick-start)
- [How it works](#how-it-works)
  - [Two layers of exception handling](#two-layers-of-exception-handling)
- [Exception classes](#exception-classes)
- [Error response format](#error-response-format)
  - [ApiError](#apierror)
  - [ValidationError](#validationerror)
- [Bean validation](#bean-validation)
  - [Request body validation](#request-body-validation)
  - [Collection validation](#collection-validation)
  - [Kotlin suspend function support](#kotlin-suspend-function-support)
- [WebFilter exception handling](#webfilter-exception-handling)
- [Logging](#logging)
- [Metrics](#metrics)
- [OpenAPI integration](#openapi-integration)
- [Building and contributing](#building-and-contributing)
- [Authors and acknowledgment](#authors-and-acknowledgment)

***

## Background: why standardized error responses?

If you are already familiar with the problem, feel free to skip to [Features](#features).

When a backend service encounters an error, the response sent to the client should be predictable.
Every endpoint should return errors in the same JSON shape, with the same fields, regardless of
whether the error came from your business logic, a database constraint, a missing request field, or
an unhandled exception. Without this, frontend code has to handle different error shapes for
different endpoints, and debugging becomes harder because logs and responses are inconsistent.

Spring WebFlux has some built-in error handling, but it returns errors in different formats depending
on where the exception originates:

| Exception source | Default Spring behavior |
|-----------------|------------------------|
| Controller method | Spring's `DefaultHandlerExceptionResolver`: inconsistent format |
| `@Valid` / `@Validated` | `WebExchangeBindException`: raw Spring format with nested field errors |
| WebFilter (e.g. auth filter) | HTML error page or generic JSON, no structured body |
| Database constraint violation | 500 Internal Server Error with a stack trace |

This library normalizes all of these into a single `ApiError` JSON format with proper HTTP status
codes, human-readable messages, and structured validation errors.

## Features

- **Sealed exception hierarchy**: throw `BadRequest("message")`, `NotFound("message")`, etc. and the correct HTTP status is set automatically
- **Consistent JSON responses**: every error returns an `ApiError` with `status`, `message`, `timestamp`, and optional `validationErrors`
- **Two-layer handling**: `RestExceptionHandler` catches controller exceptions, `WebfluxExceptionHandler` catches filter and framework exceptions
- **Bean validation** support with correct parameter names for Kotlin `suspend` functions
- **Collection validation**: `@Valid` works on `List<@Valid T>` parameters directly in controllers
- **Database conflict detection**: `DataIntegrityViolationException` is caught and returned as `409 Conflict`
- **Unhandled exception safety net**: any uncaught exception returns `500` with a generic message (no stack trace leak)
- **Debug logging**: all exceptions are logged at debug level before being returned, with configurable log levels per package
- **Optional metrics**: exception counters with type and status tags via [metrics-commons](https://github.com/phorus-group/metrics-commons), configurable per service
- **OpenAPI integration**: automatically registers `ApiError` and `ValidationError` schemas and adds error responses to all endpoints (when springdoc is on the classpath)
- **Zero configuration**: add the dependency and everything is registered via autoconfiguration

## Getting started

### Installation

Make sure that `mavenCentral` (or any of its mirrors) is added to the repository list of the project.

Binaries and dependency information for Maven and Gradle can be found at [http://search.maven.org](https://search.maven.org/search?q=g:group.phorus%20AND%20a:exception-handling).

<details open>
<summary>Gradle / Kotlin DSL</summary>

```kotlin
implementation("group.phorus:exception-handling:x.y.z")
```
</details>

<details open>
<summary>Maven</summary>

```xml
<dependency>
    <groupId>group.phorus</groupId>
    <artifactId>exception-handling</artifactId>
    <version>x.y.z</version>
</dependency>
```
</details>

### Quick start

Add the dependency and throw exceptions from your controllers:

```kotlin
@RestController
class UserController(private val userService: UserService) {

    @GetMapping("/user/{id}")
    suspend fun findById(@PathVariable id: UUID): UserResponse =
        userService.findById(id) ?: throw NotFound("User with id $id not found")

    @PostMapping("/user")
    suspend fun create(@RequestBody @Valid request: CreateUserRequest): ResponseEntity<Void> {
        val id = userService.create(request)
        return ResponseEntity.created(URI.create("/user/$id")).build()
    }
}
```

If the user is not found, the client receives:

```json
{
  "timestamp": "06-03-2026 10:30:00",
  "status": "NOT_FOUND",
  "message": "User with id 550e8400-e29b-41d4-a716-446655440000 not found"
}
```

If validation fails on the `@Valid` request body:

```json
{
  "timestamp": "06-03-2026 10:30:00",
  "status": "BAD_REQUEST",
  "message": "Validation error",
  "validationErrors": [
    {
      "obj": "createUserRequest",
      "field": "email",
      "rejectedValue": null,
      "message": "Cannot be blank"
    }
  ]
}
```

No additional configuration is needed.

## How it works

The library registers its components via Spring autoconfiguration. When you add it as a dependency,
the following beans are created automatically:

```
exception-handling (autoconfigured)
├── RestExceptionHandler: @RestControllerAdvice for controller exceptions
├── WebfluxExceptionHandler: WebExceptionHandler for filter/framework exceptions
├── WebfluxValidatorConfig: Custom validator with Kotlin suspend support
└── OpenApiAutoConfiguration: ApiError/ValidationError OpenAPI schemas (if springdoc is present)
```

### Two layers of exception handling

Spring WebFlux has two exception handling mechanisms, and this library covers both:

**`RestExceptionHandler`** (`@RestControllerAdvice`) handles exceptions thrown inside controller
methods. This covers business logic exceptions, validation errors, type mismatches, and database
constraint violations.

**`WebfluxExceptionHandler`** (`WebExceptionHandler`, `@Order(-2)`) handles exceptions thrown
*before* the request reaches a controller. For example, exceptions from WebFilters like an
authentication filter. Without this, filter exceptions would result in an HTML error page or a
generic response without a structured body.

```
Client Request
  │
  ▼
WebFilter chain ──exception──► WebfluxExceptionHandler ──► ApiError JSON
  │
  ▼
Controller ──exception──► RestExceptionHandler ──► ApiError JSON
  │
  ▼
Normal Response
```

## Exception classes

The library provides a sealed `BaseException` class and concrete subclasses for common HTTP errors:

```kotlin
throw BadRequest("Invalid input")           // 400
throw Unauthorized("Token expired")         // 401
throw Forbidden("Insufficient privileges")  // 403
throw NotFound("User not found")            // 404
throw MethodNotAllowed("Use POST")          // 405
throw RequestTimeout("Upstream timeout")    // 408
throw Conflict("Email already exists")      // 409
throw Gone("Resource deleted")             // 410
throw PreconditionFailed("ETag mismatch")   // 412
throw UnsupportedMediaType("Use JSON")      // 415
throw UnprocessableEntity("Invalid data")   // 422
throw TooManyRequests("Rate limited")       // 429
throw InternalServerError("Unexpected")     // 500
throw BadGateway("Upstream error")          // 502
throw ServiceUnavailable("Maintenance")     // 503
throw GatewayTimeout("Upstream timeout")    // 504
```

All extend `BaseException(message, httpStatus)` which extends `RuntimeException`. They can be thrown
from controllers, services, WebFilters, anywhere in your code. The handlers will catch them and
return the correct HTTP status code.

Usage examples:

```kotlin
// Throw NotFound when a resource doesn't exist
val user = userRepository.findByEmail(email)
    ?: throw NotFound("User with email $email not found")

// Throw Unauthorized on authentication failure
if (!passwordMatches) {
    throw Unauthorized("Invalid credentials")
}

// Throw Conflict on duplicate resources
if (emailExists) {
    throw Conflict("User with email $email already exists")
}

// Throw Forbidden on authorization failure
if (!hasPermission) {
    throw Forbidden("User not allowed to perform this action")
}

// Throw BadRequest on invalid input
if (!isValidFormat) {
    throw BadRequest("Invalid file format. Expected JSON")
}
```

Note that [service-commons](https://github.com/phorus-group/service-commons) also uses these
exceptions internally: its `CrudService.findById` automatically throws `NotFound` when an entity
doesn't exist.

## Error response format

### ApiError

Every error response uses the same JSON shape:

```kotlin
data class ApiError(
    val status: HttpStatus,        // Serialized as the status name, e.g. "NOT_FOUND"
    val message: String?,          // Human-readable error message
    val debugMessage: String?,     // Optional debug information
) {
    val timestamp: LocalDateTime   // When the error occurred
    val validationErrors: List<ValidationError>?  // Present only for validation failures
}
```

Example response:

```json
{
  "timestamp": "06-03-2026 10:30:00",
  "status": "BAD_REQUEST",
  "message": "Validation error",
  "validationErrors": [...]
}
```

### ValidationError

When a validation fails, each field error is represented as:

```kotlin
data class ValidationError(
    val obj: String,            // The object that originated the error
    val field: String?,         // The field name (null for global errors)
    val rejectedValue: Any?,    // The value that was rejected
    val message: String?,       // Why it was rejected
)
```

Example:

```json
{
  "obj": "userDTO",
  "field": "email",
  "rejectedValue": "",
  "message": "Cannot be blank"
}
```

## Bean validation

### Request body validation

Use `@Valid` or `@Validated` on request body parameters with Jakarta validation annotations on
your DTOs:

```kotlin
data class CreateUserRequest(
    @field:NotBlank(message = "Cannot be blank")
    val name: String?,

    @field:NotBlank(message = "Cannot be blank")
    @field:Email(message = "Invalid email format")
    val email: String?,

    @field:NotEmpty(message = "Cannot be empty")
    val subObjectList: List<SubObject>?,
)

@PostMapping("/user")
suspend fun create(@RequestBody @Valid request: CreateUserRequest) = ...
```

A key feature is that **all validation errors are collected at once** and returned in a single
response. The library doesn't stop at the first failure: it validates every field, every nested
object, and every collection item, then reports all violations together. This lets the client fix
all issues in one pass instead of playing whack-a-mole with one error at a time.

For example, if `name` is blank, `email` is null, and `subObjectList` is empty:

```json
{
  "timestamp": "06-03-2026 10:30:00",
  "status": "BAD_REQUEST",
  "message": "Validation error",
  "validationErrors": [
    {
      "obj": "createUserRequest",
      "field": "name",
      "rejectedValue": "",
      "message": "Cannot be blank"
    },
    {
      "obj": "createUserRequest",
      "field": "email",
      "rejectedValue": null,
      "message": "Cannot be blank"
    },
    {
      "obj": "createUserRequest",
      "field": "subObjectList",
      "rejectedValue": [],
      "message": "Cannot be empty"
    }
  ]
}
```

Nested object fields are reported with dot-separated paths (e.g. `subObject.testVar`), so the
client knows exactly where the error is even in deeply nested structures.

The library's `RestExceptionHandler` catches `WebExchangeBindException` and formats all field
errors into `ValidationError` objects automatically, no extra code needed.

### Collection validation

The library supports validation of items inside collections. Add `@Validated` to the controller
class and `@Valid` to the collection parameter:

```kotlin
@RestController
@Validated
class ItemController {

    @PostMapping("/items")
    suspend fun createBatch(
        @RequestBody @Valid @NotEmpty(message = "Cannot be empty")
        items: List<ItemDTO>,
    ): List<ItemResponse> = ...
}
```

Each item in the list is validated individually. If any item fails validation, the response includes
the constraint violations with proper field paths.

### Kotlin suspend function support

Spring's default parameter name discovery does not handle Kotlin `suspend` functions correctly
(the continuation parameter confuses the resolver). The library's `WebfluxValidatorConfig`
registers a custom `SuspendAwareKotlinParameterNameDiscoverer` that handles this, so validation
annotations work correctly on `suspend` controller methods without any extra configuration.

## WebFilter exception handling

Exceptions thrown in WebFilters (such as an authentication filter) are not caught by
`@RestControllerAdvice`. The library's `WebfluxExceptionHandler` (registered with `@Order(-2)`)
catches these and returns the same `ApiError` JSON format:

Example of exceptions thrown from a WebFilter:

```kotlin
@Component
class AuthenticationFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val authHeader = exchange.request.headers.getFirst("Authorization")
            ?: throw Unauthorized("Authorization header is missing")

        val token = parseToken(authHeader)
            ?: throw Unauthorized("Invalid token format")

        if (!hasRequiredPermissions(token)) {
            throw Forbidden("Insufficient permissions")
        }

        return chain.filter(exchange)
    }
}
```

All these exceptions (`Unauthorized`, `Forbidden`) are thrown *before* the request reaches any
controller. Without `WebfluxExceptionHandler`, these would produce an HTML error page. With it,
the client receives the same structured `ApiError` JSON:

```json
{
  "timestamp": "06-03-2026 10:30:00",
  "status": "UNAUTHORIZED",
  "message": "Authorization header is missing or invalid"
}
```

Authentication filters commonly throw exceptions from WebFilters, and this handler ensures they
return structured JSON responses instead of HTML error pages.

## Logging

Every exception caught by `RestExceptionHandler` and `WebfluxExceptionHandler` is logged **before**
being returned to the client. This provides visibility into errors without exposing sensitive
details to the client.

### Log levels

- **Debug level**: all exceptions (validation errors, business logic exceptions, type mismatches, etc.)
- **Error level**: only unhandled exceptions that fall through to the generic `Exception` handler

This allows you to control exception logging granularity per environment:

```yaml
# application.yml - Development: see all exceptions
logging:
  level:
    group.phorus.exception.handling: DEBUG
```

```yaml
# application.yml - Production: only log unexpected errors
logging:
  level:
    group.phorus.exception.handling: ERROR
```

### Log output examples

**Debug logs** (business exceptions):
```
DEBUG group.phorus.exception.handling.handlers.RestExceptionHandler : NotFound: User with id 550e8400-e29b-41d4-a716-446655440000 not found
DEBUG group.phorus.exception.handling.handlers.RestExceptionHandler : BadRequest: Invalid input
DEBUG group.phorus.exception.handling.handlers.WebfluxExceptionHandler : Unauthorized: Token expired
```

**Error logs** (unhandled exceptions):
```
ERROR group.phorus.exception.handling.handlers.RestExceptionHandler : Unhandled exception: NullPointerException - Cannot invoke method on null object
java.lang.NullPointerException: Cannot invoke method on null object
    at com.example.service.UserService.processUser(UserService.kt:42)
    ...
```

The library logs the exception class name and message at debug level, making it easy to track
errors in development without cluttering production logs. Unhandled exceptions (which indicate
bugs) are always logged at error level with full stack traces.

## Metrics

The library integrates with [metrics-commons](https://github.com/phorus-group/metrics-commons) to
record exception counters. When enabled, every caught exception increments a counter with metadata
tags for filtering and analysis.

### Metric details

**Metric name:** `http.server.exceptions`

**Tags:**
- `TagNames.TYPE`: exception class name (e.g., `NotFound`, `BadRequest`, `ValidationException`)
- `TagNames.STATUS_CODE`: HTTP status code (e.g., `404`, `400`, `500`)
- `TagNames.STATUS_FAMILY`: status family (e.g., `4xx`, `5xx`)

**Example queries** (Prometheus syntax):
```promql
# Rate of 404 errors
rate(http_server_exceptions_total{status_code="404"}[5m])

# Count of BadRequest exceptions
sum(http_server_exceptions_total{type="BadRequest"})

# All 5xx errors
sum(http_server_exceptions_total{status_family="5xx"})
```

### Enabling metrics

Metrics are **enabled by default** when `MeterRegistry` is on the classpath (e.g., via Spring Boot
Actuator with Micrometer). No configuration needed: the library auto-detects the registry and
starts recording.

Add Spring Boot Actuator to enable metrics:

```kotlin
implementation("org.springframework.boot:spring-boot-starter-actuator")
```

Then expose the metrics endpoint:

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
```

### Disabling metrics

If you don't want exception metrics (e.g., to reduce cardinality), disable them per service:

```yaml
# application.yml
phorus:
  exception-handling:
    metrics:
      enabled: false
```

This keeps the library installed but stops recording metrics. The exception handlers still work
normally, only the metric counters are disabled.

### Tag naming consistency

The library uses metrics-commons utilities to ensure consistent tag naming and cardinality safety:

- **`exceptionTag(throwable)`**: Safely extracts the exception's simple class name (null-safe, returns "None" if null)
- **`countStatus(name, statusCode, ...tags)`**: Automatically adds both `status_family` (e.g., "4xx") and `status_code` (e.g., "404") tags

This ensures exception metrics use the same tag names (`type`, `status_code`, `status_family`) 
as other service metrics across all Phorus libraries, making cross-service dashboards and queries easier
while preventing cardinality explosion.

## OpenAPI integration

If [springdoc-openapi](https://springdoc.org/) is on your classpath, the library automatically:

1. **Registers** the `ApiError` and `ValidationError` schemas in the OpenAPI components
2. **Adds** error responses (400, 401, 403, 404, 408, 409, 500) to every endpoint that doesn't
   already define them

This means your generated OpenAPI spec and Swagger UI will show the error response schemas without
any manual configuration. This is especially useful with code generators like
[Orval](https://orval.dev/), which can use the `ApiError` type to provide correctly typed error
handling in the frontend.

The integration is conditional: it only activates when `springdoc-openapi` classes are on the
classpath. If you don't use springdoc, nothing happens.

## Building and contributing

See [Contributing Guidelines](CONTRIBUTING.md).

## Authors and acknowledgment

Developed and maintained by the [Phorus Group](https://phorus.group) team.
