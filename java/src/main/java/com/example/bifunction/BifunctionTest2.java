package com.example.bifunction;

import java.util.HashMap;
import java.util.Map;

public class BifunctionTest2 {

    public static void main(String... strings) {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Alice", 80);
        
        // compute(key, BiFunction<key, oldValue, newValue>)
        scores.compute("Alice", (k, v) -> v + 10);  
        scores.compute("Bob", (k, v) -> (v == null ? 70 : v + 10));

        System.out.println(scores); // {Alice=90, Bob=70}

    }

}
