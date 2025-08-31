package com.example.first.ab;

/**
메시지 흐름 설정
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;

@Configuration
public class GatewayConfig {
    @Bean
    public IntegrationFlow gatewayFlow() {
        return IntegrationFlow
                .from("requestChannel")
                .transform(String.class, String::toUpperCase)
                .get();
    }
}
