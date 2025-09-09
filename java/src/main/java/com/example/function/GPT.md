원하시면 제가 주요 인터페이스 전체 목록과 사용 예제를 정리해드릴까요, 아니면 실무에서 가장 자주 쓰이는 패턴만 정리해드릴까요?

나의 말:
응 해줘
ChatGPT의 말:
1m 0s 동안 생각함

좋아요 — java.util.function 주요 인터페이스 전체 목록 + 사용 예제를 단계별(why → what → how)로 깔끔하게 정리할게요. 실무에서 바로 붙여 쓸 수 있도록 코드 + 설명 위주로 구성했어요. 🚀

1) 한눈에 보는 요약 (핵심 인터페이스)

입력→출력: Function<T,R> (apply)

입력만(사이드 이펙트): Consumer<T> (accept)

출력만(공급): Supplier<T> (get)

조건/검사: Predicate<T> (test)

쌍(두 개 인자): BiFunction<T,U,R>, BiConsumer<T,U>, BiPredicate<T,U>

같은 타입 변환: UnaryOperator<T> (extends Function<T,T>), BinaryOperator<T> (extends BiFunction<T,T,T>)

기본형 특화: IntFunction<R>, ToIntFunction<T>, IntPredicate, IntUnaryOperator, IntBinaryOperator, IntConsumer, IntSupplier, ObjIntConsumer<T> 등 (int/long/double 계열)

2) 기본 사용 예제 (각 예제에 단계별 설명 포함)
Function<T,R>
import java.util.function.Function;

Function<Integer, Integer> multiply2 = x -> x * 2;
Function<Integer, Integer> square = x -> x * x;

// andThen: multiply2 실행 후 square 실행
Function<Integer, Integer> combined = multiply2.andThen(square);

System.out.println(combined.apply(3)); // (3*2)^2 = 36


단계별:

multiply2는 입력을 2배로 만드는 함수.

square는 제곱 함수.

multiply2.andThen(square)는 먼저 2배 → 그 결과 제곱.

apply(3) → 36.

compose와 andThen 차이:

f.andThen(g) ≡ g(f(x))

f.compose(g) ≡ f(g(x))

Predicate<T> (논리 검사)
import java.util.function.Predicate;
import java.util.List;

Predicate<String> nonBlank = s -> s != null && !s.isBlank();
Predicate<String> longEnough = s -> s.length() >= 5;

Predicate<String> good = nonBlank.and(longEnough);

List<String> names = List.of("Alice", "Bob", "Charlie", "");
names.stream()
     .filter(good)
     .forEach(System.out::println); // Alice, Charlie


단계별:

기본 조건 두 개를 정의.

and, or, negate로 조합 가능.

스트림 filter에 바로 사용.

Consumer<T> (사이드 이펙트)
import java.util.function.Consumer;

Consumer<String> printer = System.out::println;
Consumer<String> exclaimer = s -> System.out.println(s + "!!!");

printer.andThen(exclaimer).accept("Hi");
// 출력: Hi
//       Hi!!!


단계별:

printer가 문자열 출력.

andThen으로 후속 동작 연쇄.

accept로 실행.

Supplier<T> (지연 공급)
import java.util.function.Supplier;
import java.util.UUID;

Supplier<UUID> uuidSupplier = UUID::randomUUID;
System.out.println(uuidSupplier.get());


추가: 메모이제이션(한 번만 생성) 예시 (스레드 안전 고려):

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

static <T> Supplier<T> memoize(Supplier<T> s) {
    AtomicReference<T> ref = new AtomicReference<>();
    return () -> {
        T v = ref.get();
        if (v == null) {
            synchronized (ref) {
                v = ref.get();
                if (v == null) {
                    v = s.get();
                    ref.set(v);
                }
            }
        }
        return v;
    };
}


단계별:

Supplier는 호출 시 값을 생성.

memoize는 처음 한 번만 생성하고 재사용(비용 큰 생성에 유용).

BiFunction / BiConsumer / BiPredicate
import java.util.function.BiFunction;
BiFunction<Integer, Integer, Integer> add = Integer::sum;
System.out.println(add.apply(3,4)); // 7

// Map.forEach 사용 예
Map<String, Integer> map = Map.of("a",1,"b",2);
map.forEach((k,v) -> System.out.println(k + ":" + v)); // BiConsumer


단계별:

두 개 인자를 받아 처리해야 할 때 사용.

Map.forEach는 BiConsumer를 받음.

UnaryOperator / BinaryOperator
import java.util.function.UnaryOperator;
UnaryOperator<String> trimAndUpper = s -> s.trim().toUpperCase();
System.out.println(trimAndUpper.apply("  hello ")); // "HELLO"


BinaryOperator<T>는 두 개 동일 타입을 입력으로 받아 동일 타입 리턴(예: Integer::sum).

기본형 특화 예제 (성능 중요)
List<String> words = List.of("a","ab","abc");
int totalLength = words.stream()
                       .mapToInt(String::length) // ToIntFunction 사용 (언박싱 방지)
                       .sum();
System.out.println(totalLength); // 6


단계별:

mapToInt는 ToIntFunction<T>를 받음.

박싱 비용 없이 IntStream으로 처리 → 성능 우수.

기타: IntPredicate, IntConsumer, IntUnaryOperator, IntBinaryOperator 등 사용 가능.

checked exception 처리 패턴 (람다에서 체크 예외)

람다 내부에서 checked 예외를 바로 던질 수 없으므로 래퍼를 씁니다.

@FunctionalInterface
interface ThrowingFunction<T,R> { R apply(T t) throws Exception; }

static <T,R> Function<T,R> wrap(ThrowingFunction<T,R> f) {
    return t -> {
        try { return f.apply(t); }
        catch (Exception e) { throw new RuntimeException(e); }
    };
}
// 사용:
Function<Path, String> read = wrap(p -> Files.readString(p));


단계별:

체크 예외를 던지는 함수형 인터페이스를 정의.

래퍼에서 체크 예외를 런타임 예외로 변환.

3) 자주 쓰는 조합 메서드 요약 (cheat)

Function<T,R>: apply(T), andThen, compose

Predicate<T>: test(T), and, or, negate

Consumer<T>: accept(T), andThen

Supplier<T>: get()

기본형: applyAsInt, test, accept, getAsInt 등

4) 실무 팁 & 흔한 실수 (요약)

성능: 반복/핫루프에서는 기본형 특화(Int/Long/Double) 사용해 박싱/언박싱 피하기.

가독성: 복잡한 람다는 메서드(또는 클래스)로 빼서 이름 붙여라 — 디버깅/테스트 쉬움.

부작용: Consumer 안에서 상태 변경(공유변수 수정)은 동시성 문제 초래 가능. 피하거나 동기화 처리.

체크 예외: 람다에서 바로 사용 불가 — wrapper 패턴 사용.

null 처리: 람다 내 null을 조심. Optional 사용 권장.

메서드 참조: Class::staticMethod, instance::method, Type::new로 가독성↑.

재사용 가능한 함수는 static final 상수로 정의해 재사용(테스트 가능).

5) 스트림과의 매핑 (한눈에)

filter → Predicate<T>

map → Function<T,R>

mapToInt / mapToLong → ToIntFunction<T> 등

forEach → Consumer<T>

reduce → BinaryOperator<T> / BiFunction (초기값+누적기)

collect → 내부적으로 Supplier, BiConsumer(accumulator), BiConsumer(combiner)

6) 빠른 레퍼런스 코드 모음 (짧은 예제들)
// Predicate 조합
Predicate<String> p = s -> s.startsWith("A");
Predicate<String> q = s -> s.length() > 3;
Predicate<String> combined = p.and(q).negate(); // not(startsWithA && len>3)

// BiFunction -> map merge 예
Map<String,Integer> counts = new HashMap<>();
BiFunction<Integer,Integer,Integer> sumFn = (a,b) -> a+b;
counts.merge("k", 1, sumFn);

// ObjIntConsumer 예
ObjIntConsumer<String> oc = (s,i) -> System.out.println(s + " -> " + i);
oc.accept("hello", 5);

7) 마무리 — 다음에 도와줄 것

원하시면:

(A) 프로젝트 코드에 바로 붙일 수 있는 유틸 파일(예: 체크예외 래퍼, memoize util, 자주 쓰는 Function 상수들)을 만들어 드릴게요.

(B) 또는 인터페이스별 한 페이지 치트시트(프린트용 Markdown/PDF) 만들어 드릴까요?

어떤 걸 먼저 해드릴까요? (원하시면 바로 코드 파일 형태로 만들어드릴게요.)