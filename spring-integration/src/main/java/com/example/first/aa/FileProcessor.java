package com.example.first.aa;

/**
비즈니스 로직 클래스 (FileProcessor.java)
이 클래스는 스프링 인티그레이션 흐름의 핵심인 비즈니스 로직을 담고 있습니다. 여기서는 파일의 내용을 단순히 대문자로 변환하는 간단한 처리를 수행합니다.

@Component 어노테이션을 통해 이 클래스를 스프링 빈으로 등록합니다.
process 메서드는 스프링 인티그레이션 흐름으로부터 File 객체를 메시지 페이로드로 받습니다.
 */
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class FileProcessor {

    public String process(File input) throws IOException {
        System.out.println("Processing file: " + input.getName());
        
        // 파일 내용을 읽고 대문자로 변환
        String content = Files.readString(Paths.get(input.getPath()));
        String transformedContent = content.toUpperCase();
        
        System.out.println("Original content: " + content);
        System.out.println("Transformed content: " + transformedContent);
        
        return transformedContent;
    }
}
