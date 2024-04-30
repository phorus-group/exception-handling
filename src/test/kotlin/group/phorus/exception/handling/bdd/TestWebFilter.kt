package group.phorus.exception.handling.bdd

import group.phorus.userservice.exceptions.Unauthorized
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebExchangeDecorator
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class TestWebFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> =
        fail(exchange.request)
            .then(chain.filter(exchange))

    private fun fail(request: ServerHttpRequest): Mono<Void> {
        if (request.path.value().contains("/failFilter"))
            throw Unauthorized("Unauthorized")

        return Mono.empty()
    }
}