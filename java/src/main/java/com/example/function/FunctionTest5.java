package com.example.function;

import java.util.function.Function;

public class FunctionTest5 {

    public static void funcCallback(String input, Function<String, String> callback) {
        String result = callback.apply(input);
        System.out.println("처리 결과: " + result);
    }

    public static void main(String... strings) {
        funcCallback("hello", str -> str.toUpperCase());  // 콜백 전달
        funcCallback("hello", str -> "[" + str + "]");    // 다른 콜백
    }    
}
