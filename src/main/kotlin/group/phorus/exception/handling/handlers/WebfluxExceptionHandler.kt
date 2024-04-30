package group.phorus.exception.handling.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import group.phorus.userservice.exceptions.BaseException
import org.slf4j.LoggerFactory
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class WebfluxExceptionHandler(
    private val objectMapper: ObjectMapper,
) : ErrorWebExceptionHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        val bufferFactory = exchange.response.bufferFactory()

        val apiError = if (ex is BaseException) {
            ApiError(ex.httpStatus, ex.message ?: ex.httpStatus.name)
        } else ApiError(HttpStatus.BAD_REQUEST, "Something failed: ${ex.message}")

        logger.error("Webflux API exception: ${ex.message}")

        val dataBuffer = bufferFactory.wrap(objectMapper.writeValueAsBytes(apiError))

        exchange.response.statusCode = apiError.status
        exchange.response.headers.contentType = MediaType.APPLICATION_JSON
        return exchange.response.writeWith(Mono.just(dataBuffer))
    }
}