package com.example.two.aa;

import org.apache.logging.log4j.message.Message;
/**
3. 스프링 인티그레이션 플로우 (IntegrationConfig.java)
이 클래스에서 모든 메시지 흐름을 정의합니다. 헤더 라우터를 사용하여 메시지를 분기하고, wireTap을 사용하여 로그를 비동기적으로 처리합니다.

MessageChannels.publishSubscribe(): 이 채널은 메시지 복사본을 생성하여 여러 소비자에게 비동기적으로 전송합니다.
.wireTap("logChannel"): inputChannel로 들어오는 모든 메시지의 복사본을 logChannel로 보냅니다. 메인 흐름은 이 작업으로 인해 차단되지 않습니다.
.route(): 메시지 헤더 operation의 값을 확인하여 흐름을 subFlowMapping으로 분기합니다.
Kafka.outboundChannelAdapter(): 메시지를 카프카로 발행하는 아웃바운드 어댑터입니다.
 */
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.MessageChannel;

@Configuration
public class IntegrationConfig {

    // 비동기 로그를 위한 채널
    @Bean
    public MessageChannel logChannel() {
        // PublishSubscribeChannel은 여러 구독자에게 메시지를 동시에 보냅니다.
        return MessageChannels.publishSubscribe().get();
    }
    
    // A 작업 처리를 위한 서비스
    public void processA(String payload) {
        System.out.println("✅ 'A' 작업 엔드포인트 실행: " + payload);
    }

    @Bean
    public IntegrationFlow mainFlow(KafkaTemplate<String, String> kafkaTemplate) {
        return IntegrationFlow
            .from("inputChannel")
            
            // wireTap으로 logChannel에 메시지 복사본을 보내 비동기 로그 처리
            .wireTap("logChannel")
            
            // 메시지 헤더 'operation'의 값에 따라 라우팅
            .<String, String>route(Message.class, m -> (String) m.getHeaders().get("operation"),
                mapping -> mapping
                    // 헤더 값이 'A'인 경우, processA 메서드 실행
                    .subFlowMapping("A", sf -> sf
                        .handle(this, "processA"))
                    
                    // 헤더 값이 'B'인 경우, 카프카로 발행
                    .subFlowMapping("B", sf -> sf
                        .handle(Kafka.outboundChannelAdapter(kafkaTemplate)
                                .topic("my-topic"))) // 발행할 카프카 토픽
            )
            .get();
    }
    
    @Bean
    public IntegrationFlow loggingFlow(@Qualifier("logChannel") MessageChannel logChannel) {
        return IntegrationFlow
            .from(logChannel) // logChannel을 구독
            .handle(message -> {
                // 비동기적으로 로그를 남기는 로직
                System.out.println("📄 비동기 로그: " + message.getHeaders().get("operation") + " 작업 처리 중");
            })
            .get();
    }
}
