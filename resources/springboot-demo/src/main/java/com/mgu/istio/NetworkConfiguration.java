package com.mgu.istio;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class NetworkConfiguration {

    @Bean
    public WebClient getWebClient() {
        return WebClient.builder()
                .filter(ExchangeFilterFunction.ofRequestProcessor(NetworkConfiguration::injectAuthorizationHeader))
                .build();
    }

    private static Mono<ClientRequest> injectAuthorizationHeader(final ClientRequest clientRequest) {
        if (SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getCredentials() != null
                && SecurityContextHolder.getContext().getAuthentication().getCredentials() instanceof Jwt jwt) {
            return Mono.just(ClientRequest
                    .from(clientRequest)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue())
                    .build());
        }
        return Mono.just(clientRequest);
    }
}
