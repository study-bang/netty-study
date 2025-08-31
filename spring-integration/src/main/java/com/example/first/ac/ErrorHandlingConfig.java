package com.example.first.ac;

/**
에러 핸들링
메시지 처리 중 예외가 발생하면, 스프링 인티그레이션은 자동으로 메시지를 **errorChannel**로 보냅니다. 개발자는 이 채널을 구독하여 공통적인 에러 처리 로직(로깅, 알림 등)을 구현할 수 있습니다.

코드 예시: 전역 에러 핸들링 플로우.
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandlingException;

@Configuration
public class ErrorHandlingConfig {

    @Bean
    public IntegrationFlow globalErrorFlow() {
        return IntegrationFlow
            .from("errorChannel") // 모든 에러 메시지가 이 채널로 들어옴
            .handle(MessageHandlingException.class, (payload, headers) -> {
                System.out.println("🚨 에러 발생! 원인: " + payload.getCause().getMessage());
                return null;
            })
            .get();
    }
}
