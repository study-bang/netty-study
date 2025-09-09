package com.example.generic;

class Box<T> {
    private T value;
    public void set(T value) {
        this.value = value;
    }
    public T get(){
        return value;
    }
}

public class GenericTest {

    public static void main(String... strings){
        // 타입 파라미터
        Box<String> box = new Box();
        box.set("Hello");
        String str = box.get();
        System.out.println(str); // Hello
    }
}