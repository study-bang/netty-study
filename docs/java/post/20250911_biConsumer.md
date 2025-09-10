### function 패키지 사용법, BiFunction 와 Map 메소드
- Bi~ 시리즈 인터페이스는 입력을 2개 받는 인터페이스이다.
- `BiFunction` / `BiConsumer` / `BiPredicate` 인터페이스이다.
- 공부를 하다보니, 생각밖으로 하나하나의 클래스마다 활용도 높은 듯하다.
- 일단 `BiFunction` 클래스부터...

#### `BiFunction<T,U,R>`
- T, U 입력을 2개 받아서 R 출력, `R = apply(T, U)`

```
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
    }
}
```

#### `Map` 메소드와 `BiFunction<T,U,R>`
1. `compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)`
- 해당 키의 현재값을 기반으로 새로운 값을 계산하여 적용
- 키가 없으면 null이 들어가서 BiFunction의 value 인자가 null일 수 있음

2. `computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)`
- 키가 존재할 때만 새로운 값을 계산하여 적용
- 키가 없으면 아무 작업도 하지 않음

3. `merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction)`
- 기존 값과 새로운 값을 병합
- 키가 없으면 value를 바로 넣음
- 키가 있으면 기존 값과 새로운 값 병합

```
import java.util.HashMap;
import java.util.Map;

public class BiFunctionMapTest {
    public static void main(String... strings) {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Bob", 90);
        scores.put("Alice", 80);
        
        // compute
        scores.compute("Alice", (key, value) -> value + 5); // key = Alice, oldValue = 80
        scores.compute("Charlie", (key, value) -> (value == null) ? 70 : value + 10); // key = Charlie, oldValue = null, NullPointerException 방지 필요
        System.out.println(scores); // {Bob=90, Alice=85, Charlie=70}

        // computeIfPresent
        scores.computeIfPresent("Charlie", (key, value) -> value * 2);
        scores.computeIfPresent("Alpha", (key, value) -> value * 0); // 키가 없으면 아무 작업 안함.
        System.out.println(scores); // {Bob=90, Alice=85, Charlie=140}

        // merge
        scores.merge("Alice", 10, (oldVal, newVal) -> oldVal + newVal); // 기존값 85 + 10
        scores.merge("David", 15, (oldVal, newVal) -> oldVal + newVal); // 키가 없으니, 15
        
        System.out.println(scores); // {Bob=90, Alice=95, Charlie=140, David=15}
    }
}
```

#### `Map` 메소드와 `Function<T,R>`
1. `computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)`
- 키가 존재하지 않으면, 값 입력
- 기존 키가 존재하면, 아무 작업 안함.

2. `getOrDefault(Object key, V defaultValue)`
- Function은 아니지만, 값을 가져올 때 기본값을 지정 가능

3. `replaceAll(BiFunction<? super K, ? super V, ? extends V> function)`
- 값 변경

```
import java.util.HashMap;
import java.util.Map;

public class FunctionMapTest {
    public static void main(String... strings) {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);

        // computeIfAbsent
        map.computeIfAbsent("b", k -> k.length() * 10); // map = {a=1, b=10}
        map.computeIfAbsent("a", k -> 100); // map = {a=1, b=10} -> 값 변경 없음, 이미 값 있는 키는 무시
        System.out.println(map);

        // getOrDefault
        int val = map.getOrDefault("c", 0);  // c가 없으면 0 반환
        System.out.println(val); // 0

        // replaceAll
        map.replaceAll((k, v) -> v * 2); // BiFunction
        System.out.println(map); // {a=2, b=20}
    }
}
```

- Map 은 자주 사용하는 객체이다 보니, 위에 메소드들을 잘 활용하면, 가독성 및 편의성으로 개발에 활용할 수 있을 것 같다.
- 머릿 속에 새겨두자.!!