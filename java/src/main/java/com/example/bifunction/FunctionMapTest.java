package com.example.bifunction;

import java.util.HashMap;
import java.util.Map;

public class FunctionMapTest {
    public static void main(String... strings) {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);

        // computeIfAbsent
        map.computeIfAbsent("b", k -> k.length() * 10); // map = {a=1, b=10}
        map.computeIfAbsent("a", k -> 100); // map = {a=1, b=10} -> 값 변경 없음, 이미 값 있는 키는 무시
        System.out.println(map);

        // getOrDefault
        int val = map.getOrDefault("c", 0);  // c가 없으면 0 반환
        System.out.println(val); // 0

        // replaceAll
        map.replaceAll((k, v) -> v * 2); // BiFunction
        System.out.println(map); // {a=2, b=20}
    }
}
