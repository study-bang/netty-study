package com.example.first.ab;

/**
게이트웨이 호출
sampleGateway.processMessage()를 호출하면, 내부적으로 requestChannel로 메시지가 보내져 처리된 후, 결과가 response 변수로 반환됩니다.
 */
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootApplication
public class Application implements CommandLineRunner {
    @Autowired
    private SampleGateway sampleGateway;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        String response = sampleGateway.processMessage("hello, gateway!");
        System.out.println("✅ 게이트웨이 응답: " + response);
    }
}
