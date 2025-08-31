package com.example.two.ab;

import org.springframework.stereotype.Component;
import org.springframework.messaging.Message;

@Component("loggingService")
public class LoggingService {
    public void log(Message<?> message) {
        System.out.println("📄 비동기 로그: " + message.getHeaders().get("operation") + " 작업 처리 중");
    }
}
