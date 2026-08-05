package com.bookstore.auth.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor internalApiKeyInterceptor(
            @Value("${app.internal-api-key}") String internalApiKey
    ) {
        return template -> template.header("X-Internal-Api-Key", internalApiKey);
    }
}
