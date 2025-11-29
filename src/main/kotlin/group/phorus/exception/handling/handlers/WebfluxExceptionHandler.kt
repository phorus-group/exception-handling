package group.phorus.exception.handling.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import group.phorus.exception.handling.BaseException
import org.slf4j.LoggerFactory
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.Mono

@Component
class WebfluxExceptionHandler(
    private val objectMapper: ObjectMapper,
) : ErrorWebExceptionHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        val bufferFactory = exchange.response.bufferFactory()

        // RestControllerAdvice handles controller exceptions
        // This only catches framework-level errors, 404s, and fallback
        val apiError = when (ex) {
            is BaseException ->
                ApiError(ex.httpStatus, ex.message ?: ex.httpStatus.name)

            is ServerWebInputException ->
                ApiError(HttpStatus.BAD_REQUEST, ex.reason ?: "Failed to read HTTP message")

            is ResponseStatusException ->
                ApiError(ex.statusCode as HttpStatus, ex.reason ?: "Request failed")

            else -> {
                logger.error("Unhandled WebFlux exception: ${ex.javaClass.simpleName}", ex)
                ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error")
            }
        }

        val dataBuffer = bufferFactory.wrap(objectMapper.writeValueAsBytes(apiError))
        exchange.response.statusCode = apiError.status
        exchange.response.headers.contentType = MediaType.APPLICATION_JSON
        return exchange.response.writeWith(Mono.just(dataBuffer))
    }
}