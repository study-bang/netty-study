package com.example.bifunction;

import java.util.function.BiFunction;

public class BifunctionTest {

    public static void main(String... strings) {

        // BiFunction<T, U, R> : 두 입력 → 하나의 출력
        BiFunction<Integer, Integer, String> sumToString = (a, b) -> "합: " + (a + b);
        System.out.println(sumToString.apply(3, 4)); // 합: 7

        BiFunction<Integer, Integer, Integer> adder = (a, b) -> a + b;
        System.out.println(adder.apply(3, 5)); // 8

        BiFunction<String, Integer, String> repeater = (s, n) -> s.repeat(n);
        System.out.println(repeater.apply("hi", 3)); // hihihi

        // // BiConsumer<T, U> : 두 입력 → 출력 없음 (side effect)
        // BiConsumer<String, Integer> printer = (name, age) -> System.out.println(name + "의 나이는 " + age + "세");
        // printer.accept("Alice", 30);

        // // BiPredicate<T, U> : 두 입력 → boolean
        // BiPredicate<String, String> equalsIgnoreCase = (s1, s2) -> s1.equalsIgnoreCase(s2);
        // System.out.println(equalsIgnoreCase.test("hello", "HELLO")); // true

        // BiPredicate<Integer, Integer> greaterThan = (a, b) -> a < b;
        // System.out.println(greaterThan.test(5, 3)); // false

        // // UnaryOperator<T> : Function<T,T>의 특화 버전 (입출력 타입 같음)
        // UnaryOperator<String> toUpper = String::toUpperCase;
        // System.out.println(toUpper.apply("hello")); // HELLO

        // // BinaryOperator<T> : BiFunction<T,T,T>의 특화 버전
        // BinaryOperator<Integer> max = Integer::max;
        // System.out.println(max.apply(10, 20)); // 20



    }

}
