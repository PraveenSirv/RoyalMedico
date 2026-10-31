package com.medical.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class SecurityHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(Jwt.class)
                .map(jwt -> {
                    String userId = jwt.getClaimAsString("userId");
                    String username = jwt.getClaimAsString("sub");
                    String role = jwt.getClaimAsString("role");
                    String tenantId = jwt.getClaimAsString("tenantId");
                    String membershipId = jwt.getClaimAsString("membershipId");

                    ServerHttpRequest.Builder builder = exchange.getRequest().mutate()
                            .header("X-Authenticated-UserId", userId != null ? userId : "")
                            .header("X-Authenticated-User", username != null ? username : "")
                            .header("X-Authenticated-Role", role != null ? role : "");

                    if (tenantId != null && !tenantId.isBlank()) {
                        builder.header("X-Authenticated-TenantId", tenantId);
                    }
                    if (membershipId != null && !membershipId.isBlank()) {
                        builder.header("X-Authenticated-MembershipId", membershipId);
                    }

                    return exchange.mutate().request(builder.build()).build();
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        // Run after Spring Security filters but before routing
        return 0;
    }
}
