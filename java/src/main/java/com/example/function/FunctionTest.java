package com.example.function;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FunctionTest {

    public static void main(String... strings) {
        
        // Function<T, R>: T 타입 입력 받아 R 타입 리턴
        Function<String, Integer> stringLength = str -> str.length();
        System.out.println(stringLength.apply("Hello")); // 5

        // Consumer<T>: T 타입 입력 받아 소비(리턴 없음)
        Consumer<String> printer = str -> System.out.println(str);
        printer.accept("Hello World");

        // Supplier<T>: 아무 입력도 없고 T 타입 리턴
        Supplier<Long> timeSupplier = () -> System.currentTimeMillis();
        System.out.println(timeSupplier.get());

        // Predicate<T>: T 타입 입력 받아 boolean 반환
        Predicate<String> isEmpty = str -> str.isEmpty();
        System.out.println(isEmpty.test(""));   // true
        System.out.println(isEmpty.test("hi")); // false
    }    
}
