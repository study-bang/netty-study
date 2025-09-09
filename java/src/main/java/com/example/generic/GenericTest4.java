package com.example.generic;

import java.util.ArrayList;
import java.util.List;

public class GenericTest4 {

    public static void main(String... strings){
        // 와일드카드(?)
        // <?>:어떤 타입이든 허용
        // <? extends T>:T 또는 T의 하위타입
        // <? super T>:T 또는 T의 상위타입
        List<?> list = new ArrayList<String>();
        List<? extends Number> list2 = new ArrayList<Integer>();
        List<? super Integer> list3 = new ArrayList<Number>();        
    }
}