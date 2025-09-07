#### **메타 애노테이션(Meta-Annotation)
- 애노테이션(Annotation): 클래스, 메서드, 필드 등에 붙여서 특별한 의미나 동작을 부여하는 것
    - 예: `@Service`, `@Autowired`, `@GetMapping`
- 메타 애노테이션(Meta-Annotation): 다른 애노테이션을 정의할 때 사용하는 애노테이션
- 자바에서 흔히 쓰는 메타 애노테이션
    - @Target : 이 애노테이션을 어디에 적용할 수 있는지 지정 (클래스, 메서드, 필드 등)
    - @Retention : 언제까지 유지할지 지정 (소스, 클래스, 런타임)
    - @Documented : Javadoc에 포함할지 여부
    - @Inherited : 상속 가능 여부
    ```
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MyAnnotation {}
    ```
    - `@Target`, `@Retention`: 메타 애노테이션(meta-annotation)
    - `@MyAnnotation`: 커스텀 애노테이션(custom annotation)

- 스프링에서의 메타 애노테이션
    - 스프링에서는 애노테이션 위에 또 애노테이션을 붙여서 새로운 커스텀 애노테이션(custom annotation)을 만들 수 있다.
    - 예: `@RestController`는 사실 `@Controller` + `@ResponseBody`가 합쳐진 메타 애노테이션
    ```
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Controller
    @ResponseBody
    public @interface RestController {}
    ```
- 정리
    - 메타 애노테이션 = 애노테이션을 정의할 때 쓰이는 애노테이션
    - 자바 기본: @Target, @Retention 등
    - 스프링: 기존 애노테이션을 조합해서 새로운 의미를 가진 애노테이션을 만드는 것
    - 장점: 코드 중복 줄이고, 도메인 친화적인 선언 가능
---