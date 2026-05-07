package com.medical.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();
        
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        ServerHttpRequest mutatedRequest = request.mutate()
                .header(CORRELATION_ID_HEADER, correlationId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        String path = mutatedRequest.getURI().getPath();
        String method = mutatedRequest.getMethod().name();
        String finalCorrelationId = correlationId;

        return chain.filter(mutatedExchange).then(Mono.fromRunnable(() -> {
            long executionTime = System.currentTimeMillis() - startTime;
            int statusCode = mutatedExchange.getResponse().getStatusCode() != null 
                    ? mutatedExchange.getResponse().getStatusCode().value() : 0;
            
            log.info("RequestTrace [CorrelationId={}, Method={}, URI={}, Status={}, Time={}ms]",
                    finalCorrelationId, method, path, statusCode, executionTime);
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
