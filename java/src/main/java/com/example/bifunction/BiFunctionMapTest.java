package com.example.bifunction;

import java.util.HashMap;
import java.util.Map;

public class BiFunctionMapTest {
    public static void main(String... strings) {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Bob", 90);
        scores.put("Alice", 80);
        
        // compute
        scores.compute("Alice", (key, value) -> value + 5); // key = Alice, oldValue = 80
        scores.compute("Charlie", (key, value) -> (value == null) ? 70 : value + 10); // key = Charlie, oldValue = null, NullPointerException 방지 필요
        System.out.println(scores); // {Bob=90, Alice=85, Charlie=70}

        // computeIfPresent
        scores.computeIfPresent("Charlie", (key, value) -> value * 2);
        scores.computeIfPresent("Alpha", (key, value) -> value * 0); // 키가 없으면 아무 작업 안함.
        System.out.println(scores); // {Bob=90, Alice=85, Charlie=140}

        // merge
        scores.merge("Alice", 10, (oldVal, newVal) -> oldVal + newVal); // 기존값 85 + 10
        scores.merge("David", 15, (oldVal, newVal) -> oldVal + newVal); // 키가 없으니, 15
        
        System.out.println(scores); // {Bob=90, Alice=95, Charlie=140, David=15}
    }
}
