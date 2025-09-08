package function;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class MainTest {

    public static void funcEx() {

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

    public static void baseFuncEx() {
        
        // IntFunction<R>, IntConsumer, IntPredicate, IntSupplier        
        IntFunction intFunction = num -> num;
        System.out.println(intFunction.apply(5)); // 5

        java.util.function.IntConsumer IntConsumer = num -> System.out.println(num);
        IntConsumer.accept(2); // 2

        IntSupplier intSupplier = () -> 0;
        System.out.println(intSupplier.getAsInt());
        
        IntPredicate isEven = num -> num % 2 == 0;
        System.out.println(isEven.test(4)); // true
    }

    public static void funcEtc() {
        // 조합 (andThen, compose, etc.)
        Function<Integer, Integer> multiply2 = x -> x * 2;
        Function<Integer, Integer> square = x -> x * x;

        Function<Integer, Integer> combined = multiply2.andThen(square);
        System.out.println(combined.apply(3)); // (3*2)^2 = 36 // 3*2 = 6 -> 6 * 6 = 36 
    }

    public static void funcStream() {
        // 스트림 map, filter, forEach
        List<String> names = List.of("Alice", "Bob", "Charlie");

        // filter (Predicate), map (Function), forEach (Consumer)
        names.stream()
            .filter(name -> name.length() > 3)  // Predicate
            .map(String::toUpperCase)           // Function
            .forEach(System.out::println);      // Consumer
    }
    
    public static void funcCallback(String input, Function<String, String> callback) {
        String result = callback.apply(input);
        System.out.println("처리 결과: " + result);
    }

    public static void main(String... strings) {
        funcEx();
        baseFuncEx();
        funcEtc();
        funcStream();
        funcCallback("hello", str -> str.toUpperCase());  // 콜백 전달
        funcCallback("hello", str -> "[" + str + "]");    // 다른 콜백
    }    
}
