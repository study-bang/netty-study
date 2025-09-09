package com.example.generic;

public class GenericTest2 {
    public static <T> T echo(T input) {
        return input;
    }

    public static void main(String... strings){

        // 제네릭 메소드
        String str2 = echo("hello");
        int i = echo(0);
        System.out.println(str2); // hello
        System.out.println(i); // 0
    }
}
