package com.example.first.ab;

/**
게이트웨이를 이용한 동기적 연동
게이트웨이는 내부의 비동기적 메시지 흐름을 동기적인 메서드 호출처럼 보이게 만듭니다.
 */
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway
public interface SampleGateway {
    @Gateway(requestChannel = "requestChannel")
    String processMessage(String message);
}
