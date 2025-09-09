package com.example.function;

import java.util.List;

public class FunctionTest4 {

    public static void main(String... strings) {
        // 스트림 map, filter, forEach
        List<String> names = List.of("Alice", "Bob", "Charlie");

        // filter (Predicate), map (Function), forEach (Consumer)
        names.stream()
            .filter(name -> name.length() > 3)  // Predicate
            .map(String::toUpperCase)           // Function
            .forEach(System.out::println);      // Consumer
    }    
}
