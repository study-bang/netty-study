### function 패키지 사용법, 확장형, Bi- 시리즈 인터페이스
- 입력을 2개 받는 인터페이스이다.

#### `BiFunction` / `BiConsumer` / `BiPredicate`
1. `BiFunction<T,U,R>`
- T, U 입력을 2개 받아서 R 출력, `R = apply(T, U)`

```
        // BiFunction<T, U, R> : 두 입력 → 하나의 출력
        BiFunction<Integer, Integer, String> sumToString = (a, b) -> "합: " + (a + b);
        System.out.println(sumToString.apply(3, 4)); // 합: 7

        BiFunction<Integer, Integer, Integer> adder = (a, b) -> a + b;
        System.out.println(adder.apply(3, 5)); // 8

        BiFunction<String, Integer, String> repeater = (s, n) -> s.repeat(n);
        System.out.println(repeater.apply("hi", 3)); // hihihi
```

2. `BiConsumer<T,U>`
- T, U 입력을 2개 받아서 출력 없음(void), `accept(T, U)`
- 주로 출력, 저장, 로그 기록, 상태 변경 같은 동작에서 사용됨.

```
        // BiConsumer<T, U> : 두 입력 → 출력 없음 (side effect)
        BiConsumer<String, Integer> printer = (name, age) -> System.out.println(name + "의 나이는 " + age + "세");
        printer.accept("Alice", 30);
```

3. `BiPredicate<T,U>`
- T, U 입력을 2개 받아서 값을 비교/검증, boolean 반환, 조건/검사, `boolean = test(T, U)` 실행

```
        // BiPredicate<T, U> : 두 입력 → boolean
        BiPredicate<String, String> equalsIgnoreCase = (s1, s2) -> s1.equalsIgnoreCase(s2);
        System.out.println(equalsIgnoreCase.test("hello", "HELLO")); // true

        BiPredicate<Integer, Integer> greaterThan = (a, b) -> a < b;
        System.out.println(greaterThan.test(5, 3)); // false
```

📌 Java 함수형 인터페이스 한눈에 보기
구분	인터페이스	입력	출력	설명
기본형	Function<T, R>	T	R	입력 1 → 출력 1
	Consumer<T>	T	없음	입력 1 → 부수효과(side effect)
	Supplier<R>	없음	R	입력 없음 → 출력 1
	Predicate<T>	T	boolean	조건 검사 (true/false)
🔹 Bi 계열 (입력 2개 받는 경우)
인터페이스	입력	출력	설명
BiFunction<T, U, R>	T, U	R	두 입력 → 출력
BiConsumer<T, U>	T, U	없음	두 입력 → 부수효과
BiPredicate<T, U>	T, U	boolean	두 입력 조건 검사
🔹 Operator 계열 (입출력이 같은 타입)
인터페이스	입력	출력	설명
UnaryOperator<T>	T	T	단항 연산자 (예: 대문자 변환)
BinaryOperator<T>	T, T	T	이항 연산자 (예: 합, 최댓값)
🔹 원시 타입 특화형 (성능 최적화용)
예시	설명
IntSupplier, IntConsumer, IntFunction<R>, IntPredicate	int 전용
LongSupplier, LongConsumer, LongPredicate	long 전용
DoubleSupplier, DoubleConsumer, DoublePredicate	double 전용

👉 이유: 제네릭은 int, double 같은 원시타입 직접 지원 불가 → 오토박싱 비용 줄이기 위해 제공됨.

🔹 조합 메서드
인터페이스	메서드	설명
Function<T,R>	.andThen(), .compose()	여러 변환 연쇄
Predicate<T>	.and(), .or(), .negate()	조건 결합
Consumer<T>	.andThen()	여러 소비자 결합

📖 정리

핵심 4개 → Function, Consumer, Supplier, Predicate
확장형 → BiFunction, BiConsumer, BiPredicate
특화형 → UnaryOperator, BinaryOperator
성능형 → Int/Long/Double 계열
체이닝 메서드 → 함수형 인터페이스끼리 조합 가능



---
BiFunction, BiConsumer, BiPredicate 같은 Bi 계열 인터페이스는 입력을 2개 받기 때문에 Map 처리, 데이터 검증, 로깅 같은 상황에서 유용하게 쓰입니다.

하나씩 예시로 보여드릴게요 👇

1. BiFunction<T,U,R> → Map 처리

Map 의 compute, merge 같은 메서드가 BiFunction 을 받습니다.

import java.util.*;

public class BiFunctionExample {
    public static void main(String[] args) {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Alice", 80);

        // compute(key, BiFunction<key, oldValue, newValue>)
        scores.compute("Alice", (k, v) -> v + 10);  
        scores.compute("Bob", (k, v) -> (v == null ? 70 : v + 10));

        System.out.println(scores); // {Alice=90, Bob=70}
    }
}


👉 BiFunction 으로 key, value를 동시에 받아 새로운 값 산출 가능.

2. BiPredicate<T,U> → 데이터 검증기

두 입력을 받아 조건 검증할 때 사용.

import java.util.function.BiPredicate;

public class BiPredicateExample {
    public static void main(String[] args) {
        BiPredicate<String, String> isValidLogin = 
            (id, pw) -> id.equals("admin") && pw.length() >= 4;

        System.out.println(isValidLogin.test("admin", "1234")); // true
        System.out.println(isValidLogin.test("user", "12"));    // false
    }
}


👉 ID + PW 같이 2개 값 검증 시 적합.

3. BiConsumer<T,U> → 로깅

두 입력을 받아서 단순히 출력/저장 같은 부수효과를 낼 때.

import java.util.function.BiConsumer;

public class BiConsumerExample {
    public static void main(String[] args) {
        BiConsumer<String, Integer> logger = 
            (name, age) -> System.out.println("User: " + name + ", Age: " + age);

        logger.accept("Alice", 30);
        logger.accept("Bob", 25);
    }
}


👉 로깅, 디버깅, DB insert 같은 곳에서 자주 쓰임.

✅ 정리

BiFunction<T,U,R> → Map 같은 key/value 연산 (출력 있음)

BiPredicate<T,U> → 조건 검증 (true/false 반환)

BiConsumer<T,U> → 로깅/저장 같은 사이드 이펙트 작업 (출력 없음)

👉 원하시면 제가 이 3개를 Map 처리 + 검증 + 로깅을 한 흐름으로 연결한 샘플 코드도 작성해드릴까요?