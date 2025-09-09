package com.example.function;

import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;

public class FunctionTest2 {
    
    public static void main(String... strings) {

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
}
