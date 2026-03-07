package group.phorus.exception.handling.handlers

import tools.jackson.databind.ObjectMapper
import group.phorus.exception.handling.BaseException
import group.phorus.exception.handling.config.MetricsRecorder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.web.server.WebExceptionHandler
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.core.annotation.Order
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.Mono

/**
 * WebFlux exception handler that catches exceptions thrown **before** reaching controller methods,
 * such as exceptions from WebFilters, routing errors, and framework-level failures.
 *
 * This handler is registered with `@Order(-2)` to run early in the WebFlux exception handling chain.
 * It converts all caught exceptions to standardized [ApiError] JSON responses.
 *
 * **Why this is needed?**
 * `@RestControllerAdvice` (used by [RestExceptionHandler]) only catches exceptions thrown
 * **inside** controller methods. Exceptions from WebFilters (e.g., authentication filters,
 * rate limiting filters) occur before the request reaches a controller, so they bypass
 * `@RestControllerAdvice`. Without this handler, filter exceptions would result in generic framework responses.
 *
 * **Example:** An authentication filter may throw `Unauthorized` or `Forbidden` from a WebFilter.
 * This handler catches those and returns structured JSON.
 *
 * All caught exceptions are logged at **debug level** (configurable via `application.yml`)
 * and optionally recorded as metrics (see [MetricsRecorder]).
 *
 * **Logging configuration:**
 * ```yaml
 * logging:
 *   level:
 *     group.phorus.exception.handling: DEBUG  # Shows all exception logs
 * ```
 *
 * @see RestExceptionHandler for controller-level exception handling
 * @see MetricsRecorder for optional metrics recording
 */
@AutoConfiguration
@Order(-2)
class WebfluxExceptionHandler(
    private val objectMapper: ObjectMapper,
    metricsProvider: ObjectProvider<MetricsRecorder>,
) : WebExceptionHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val metrics = metricsProvider.getIfAvailable()

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        val bufferFactory = exchange.response.bufferFactory()

        // RestControllerAdvice handles controller exceptions
        // This only catches framework-level errors, 404s, and fallback
        val apiError = when (ex) {
            is BaseException -> {
                logger.debug("{}: {}", ex.javaClass.simpleName, ex.message)
                metrics?.record(ex, ex.httpStatus)
                ApiError(ex.httpStatus, ex.message ?: ex.httpStatus.name)
            }

            is ServerWebInputException -> {
                logger.debug("Server web input error: {}", ex.reason)
                metrics?.record(ex, HttpStatus.BAD_REQUEST)
                ApiError(HttpStatus.BAD_REQUEST, ex.reason ?: "Failed to read HTTP message")
            }

            is ResponseStatusException -> {
                val httpStatus = HttpStatus.valueOf(ex.statusCode.value())
                logger.debug("Response status exception: status={}, reason={}", httpStatus, ex.reason)
                metrics?.record(ex, httpStatus)
                ApiError(httpStatus, ex.reason ?: "Request failed")
            }

            else -> {
                logger.error("Unhandled WebFlux exception: ${ex.javaClass.simpleName}", ex)
                metrics?.record(ex, HttpStatus.INTERNAL_SERVER_ERROR)
                ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error")
            }
        }

        val dataBuffer = bufferFactory.wrap(objectMapper.writeValueAsBytes(apiError))
        exchange.response.statusCode = apiError.status
        exchange.response.headers.contentType = MediaType.APPLICATION_JSON
        return exchange.response.writeWith(Mono.just(dataBuffer))
    }
}
