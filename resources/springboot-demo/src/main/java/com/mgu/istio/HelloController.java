package com.mgu.istio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class HelloController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloController.class);

    private WebClient webClient;

    @Value("${invoked.url}")
    private String targetUrl;

    public HelloController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/public/hello")
    public String publicHello() {
        return "hello world";
    }

    @GetMapping("/hello")
    @PreAuthorize("hasRole('P_XXX_MGU')") // uncomment this line if the authorities are in a custom roles section of the JWT
//    @PreAuthorize("hasAuthority('ROLE_P_XXX_MGU')") // uncomment this line if the authorities are in the scope section of the JWT
    public String hello() {
        return "hello world";
    }

    @GetMapping("/me")
    public Object me() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @GetMapping("/me2")
    public Object me2(@AuthenticationPrincipal Jwt jwt) {
        return new UserInfo(jwt.getSubject(),
                "N/A", jwt.getClaimAsStringList("roles"), "N/A", "N/A", "N/A");
    }
    @GetMapping("/micro")
    public Mono<String> invokeOtherEndpointWithWebClient() {
        LOGGER.info("/micro webclient invoked, calling " + targetUrl);
        return webClient.get().uri(targetUrl)
                .exchangeToMono(response -> response.statusCode() == HttpStatus.OK
                        ? response.bodyToMono(String.class)
                                .zipWith(Mono.just("micro service invoking " + targetUrl + ", ms response is: "),
                                        (msResponse, monoString) -> monoString + msResponse)
                        : Mono.just("micro service (invoked service response code is:"
                                + response.statusCode()
                                + ") calling "
                                + targetUrl));
    }
}
