package com.example.function;

import java.util.function.Function;

public class FunctionTest3 {

    public static void main(String... strings) {

        // 조합 (andThen, compose, etc.)
        Function<Integer, Integer> multiply2 = x -> x * 2;
        Function<Integer, Integer> square = x -> x * x;

        Function<Integer, Integer> andThenTest = multiply2.andThen(square);
        System.out.println(andThenTest.apply(3)); // 3*2 = 6 -> 6 * 6 = 36 
        
        Function<Integer, Integer> composeTest = multiply2.compose(square);
        System.out.println(composeTest.apply(3)); // 3*3 = 9 -> 9 * 2 = 18
    }    
}
