package com.example.first.ac;

/**
2. 라우터 (Router) 🚦
라우터는 메시지의 페이로드나 헤더 정보를 기반으로 메시지를 여러 개의 다른 채널 중 하나로 라우팅합니다. 조건에 따라 메시지 흐름을 분기시킬 때 사용합니다.

코드 예시: 주문 타입(정상 주문, 반품)에 따라 메시지를 다른 채널로 보내는 라우터.

.route(): 메시지 라우팅을 시작하는 DSL 메서드입니다. 첫 번째 인자는 라우팅 기준을 제공하는 함수이고, 두 번째 인자는 매핑 정보를 정의하는 람다식입니다.
.subFlowMapping(): 특정 라우팅 기준(파일 이름)에 맞는 메시지를 다른 서브플로우로 보냅니다.
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files;
import java.io.File;

@Configuration
public class RouterIntegrationConfig {
    
    @Bean
    public IntegrationFlow orderRouterFlow() {
        return IntegrationFlow
            .from(Files.inboundAdapter(new File("order-router-dir")), 
                  e -> e.poller(Pollers.fixedDelay(1000)))
            
            // 메시지 페이로드에 따라 채널 분기
            .<File, String>route(File::getName, // 파일 이름을 라우팅 기준으로 사용
                mapping -> mapping
                    .subFlowMapping("new_order.txt", sf -> sf
                        .handle(file -> System.out.println("✅ 신규 주문 처리: " + file.getName())))
                    .subFlowMapping("return.txt", sf -> sf
                        .handle(file -> System.out.println("❌ 반품 주문 처리: " + file.getName())))
                    .defaultOutputToParentFlow())
            .get();
    }
}
