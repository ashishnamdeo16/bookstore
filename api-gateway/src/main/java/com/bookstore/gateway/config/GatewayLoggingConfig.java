package com.bookstore.gateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayLoggingConfig {

    @Bean
    public GlobalFilter requestLoggingFilter() {
        return (exchange, chain) -> {

            System.out.println(
                    "REQUEST REACHED API GATEWAY: "
                            + exchange.getRequest().getMethod()
                            + " "
                            + exchange.getRequest().getURI()
            );

            return chain.filter(exchange);
        };
    }
}