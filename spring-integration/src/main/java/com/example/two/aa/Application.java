package com.example.two.aa;

/**
4. 실행 및 동작 확인 (Application.java)
스프링 부트 애플리케이션을 실행하고, 게이트웨이를 통해 메시지를 보내어 동작을 확인합니다.

inputChannel을 통해 메시지를 전송하면, 헤더 값에 따라 processA 메서드가 실행되거나 카프카로 메시지가 발행됩니다.
이와 동시에, logChannel을 구독하는 loggingFlow가 비동기적으로 로그를 남기는 것을 콘솔에서 확인할 수 있습니다.
 */
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.Message;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class Application implements CommandLineRunner {

    // inputChannel을 주입받아 메시지를 보냅니다.
    @Autowired
    private MessageChannel inputChannel;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException {
        // 헤더 'operation'에 'A'를 담은 메시지 전송
        Message<String> messageA = MessageBuilder.withPayload("메시지 A").setHeader("operation", "A").build();
        inputChannel.send(messageA);
        
        TimeUnit.SECONDS.sleep(2); // 로그가 출력될 시간을 줍니다.

        // 헤더 'operation'에 'B'를 담은 메시지 전송
        Message<String> messageB = MessageBuilder.withPayload("메시지 B").setHeader("operation", "B").build();
        inputChannel.send(messageB);
        
        TimeUnit.SECONDS.sleep(2);
    }
}
