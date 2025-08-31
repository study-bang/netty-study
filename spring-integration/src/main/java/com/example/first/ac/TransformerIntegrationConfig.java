package com.example.first.ac;

/**
트랜스포머 (Transformer) 🔄
트랜스포머는 메시지의 페이로드 형식을 변환하는 역할을 합니다. 서로 다른 데이터 형식을 사용하는 시스템 간에 데이터를 교환할 때 필수적입니다.

코드 예시: 파일 내용을 File 객체에서 String으로 변환하는 트랜스포머.
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files;
import java.io.File;

@Configuration
public class TransformerIntegrationConfig {
    
    @Bean
    public IntegrationFlow fileToTextFlow() {
        return IntegrationFlow
            .from(Files.inboundAdapter(new File("transformer-dir")), 
                  e -> e.poller(Pollers.fixedDelay(1000)))
            
            // File 객체 페이로드를 String으로 변환
            .transform(File.class, file -> {
                try {
                    return new String(Files.readAllBytes(file.toPath()));
                } catch (IOException e) {
                    throw new RuntimeException("파일 변환 실패", e);
                }
            })
            
            .handle(text -> {
                System.out.println("✅ 파일 내용(문자열) 처리: " + text);
            })
            .get();
    }
}
