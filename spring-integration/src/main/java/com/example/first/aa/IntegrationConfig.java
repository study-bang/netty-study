package com.example.first.aa;

/**
스프링 인티그레이션 설정 클래스 (IntegrationConfig.java)
이 클래스에서 스프링 인티그레이션의 전체 메시지 흐름을 자바 DSL을 사용하여 정의합니다. @Configuration 클래스로, 애플리케이션 시작 시 자동으로 설정이 로드됩니다.

from(): Files.inboundAdapter를 사용하여 input-dir 디렉터리를 1초마다 주기적으로 확인(polling)합니다. 새로운 파일이 감지되면, 해당 파일을 페이로드로 하는 메시지를 생성합니다.

handle() (서비스 액티베이터): 이전 단계에서 생성된 메시지를 fileProcessor 빈의 process 메서드로 전달합니다. process 메서드는 파일 내용을 대문자로 변환한 문자열을 반환합니다. 이 반환 값이 다음 메시지의 페이로드가 됩니다.

handle() (아웃바운드 어댑터): Files.outboundAdapter는 이전 단계에서 반환된 문자열 페이로드를 output-dir 디렉터리에 새로운 파일로 저장합니다.
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow; // 변경된 부분: IntegrationFlows -> IntegrationFlow
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.support.FileExistsMode;
import java.io.File;

@Configuration
public class IntegrationConfig {

    @Bean
    public IntegrationFlow fileProcessingFlow(FileProcessor fileProcessor) {
        return IntegrationFlow
            // 1. 인바운드 어댑터: 'input-dir' 디렉터리 감시
            .from(Files.inboundAdapter(new File("input-dir"))
                        .preventDuplicates(true), 
                  e -> e.poller(Pollers.fixedDelay(1000))) // 1초마다 디렉토리 확인
            
            // 2. 서비스 액티베이터: 메시지 페이로드를 fileProcessor 빈의 'process' 메서드로 전달
            .handle(fileProcessor, "process")
            
            // 3. 아웃바운드 어댑터: 처리된 결과를 'output-dir'에 파일로 저장
            .handle(Files.outboundAdapter(new File("output-dir"))
                        .fileExistsMode(FileExistsMode.REPLACE))
            .get();
    }
}
