package com.example.generic;

// 제네릭 인터페이스
interface Transformer<T, R> {
    R transform(T input);
}

public class GenericTest3 implements Transformer<String, Integer> {
    public Integer transform(String input) {
        return input.length();
    }

    public static void main(String... strings){
        Integer length = new GenericTest3().transform("hello");
        System.out.println(length); // 5
    }
}