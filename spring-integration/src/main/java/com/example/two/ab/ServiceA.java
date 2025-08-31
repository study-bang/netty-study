package com.example.two.ab;

import org.springframework.stereotype.Component;

@Component("serviceA")
public class ServiceA {
    public void processA(String payload) {
        System.out.println("✅ 'A' 작업 엔드포인트 실행: " + payload);
    }
}