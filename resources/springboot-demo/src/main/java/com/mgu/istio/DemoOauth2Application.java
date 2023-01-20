package com.mgu.istio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication(exclude = {SessionAutoConfiguration.class})
public class DemoOauth2Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloController.class);

    @Value("${spring.security.oauth2.resource-server.jwt.issuer-uri}")
    private String issuerUrl;

    @Value("${invoked.url}")
    private String targetUrl;

    public static void main(String[] args) {
        SpringApplication.run(DemoOauth2Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        LOGGER.info("MGU >> target endpoint is " + targetUrl);
        LOGGER.info("MGU >> trusted issuer URL is " + issuerUrl);
    }
}
