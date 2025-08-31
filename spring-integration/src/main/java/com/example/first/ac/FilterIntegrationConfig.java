package com.example.first.ac;

/**
1. 필터 (Filter) 🚫
필터는 특정 조건을 만족하는 메시지만 다음 단계로 통과시키고, 그렇지 않은 메시지는 무시하거나 다른 채널로 보내는 역할을 합니다. 유효성 검증이나 특정 조건에 따라 메시지를 분리해야 할 때 유용합니다.

코드 예시: 주문 금액이 0보다 큰 메시지만 다음 단계로 진행시키는 필터.
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files;
import java.io.File;
// import java.nio.file.Files;

@Configuration
public class FilterIntegrationConfig {

    @Bean
    public IntegrationFlow orderProcessingFlow() {
        return IntegrationFlow
            .from(Files.inboundAdapter(new File("order-input-dir")), 
                  e -> e.poller(Pollers.fixedDelay(1000)))
            
            // 파일 내용(주문 금액)이 0보다 큰 메시지만 통과시킴
            .filter(ofile -> {
                try {
                    File file = (File) ofile;
                    String content = java.nio.file.Files.readString(file.toPath());
                    double amount = Double.parseDouble(content);
                    return amount > 0;
                } catch (Exception e) {
                    return false; // 파싱 오류 시 필터링
                }
            })
            
            .handle(file -> {
                System.out.println("✅ 유효한 주문 메시지 처리: " + file.getName());
            })
            .get();
    }
}