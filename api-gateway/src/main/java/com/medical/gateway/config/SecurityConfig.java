package com.medical.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        // PUBLIC
                        .pathMatchers("/api/auth/refresh").permitAll()
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers("/actuator/health").permitAll()
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // PLATFORM ADMINISTRATION ONLY (SUPER_ADMIN)
                        .pathMatchers("/api/admin/**").hasRole("SUPER_ADMIN")
                        .pathMatchers("/actuator/**").hasRole("SUPER_ADMIN")

                        // TENANT PHARMACY OPERATIONS (OWNER & PHARMACIST)
                        .pathMatchers("/api/inventory/manage/**").hasAnyRole("OWNER", "PHARMACIST")
                        .pathMatchers("/api/supplier/manage/**").hasAnyRole("OWNER", "PHARMACIST")
                        .pathMatchers("/api/medicine/**").hasAnyRole("OWNER", "PHARMACIST")
                        .pathMatchers("/api/prescription/**").hasAnyRole("OWNER", "PHARMACIST")
                        .pathMatchers("/api/orders/process/**").hasAnyRole("OWNER", "PHARMACIST")

                        // CUSTOMER OPERATIONS
                        .pathMatchers("/api/orders/**").hasAnyRole("CUSTOMER", "OWNER", "PHARMACIST")
                        .pathMatchers("/api/cart/**").hasRole("CUSTOMER")
                        .pathMatchers("/api/profile/**").authenticated()

                        // DELIVERY AGENT
                        .pathMatchers("/api/delivery/**").hasRole("DELIVERY_AGENT")

                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }

    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Object role = jwt.getClaims().get("role");
            if (role != null) {
                return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toString()));
            }

            Object authorities = jwt.getClaims().get("authorities");
            if (authorities instanceof List) {
                return ((List<?>) authorities).stream()
                        .map(auth -> new SimpleGrantedAuthority(auth.toString()))
                        .collect(Collectors.toList());
            }

            return Collections.emptyList();
        });
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}
