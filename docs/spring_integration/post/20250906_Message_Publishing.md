# Spring Integration
## [메시지 발행 (Message Publishing)](https://docs.spring.io/spring-integration/reference/message-publishing.html)
**AOP(Aspect-oriented Programming) 메시지 발행 기능**은 메서드 호출의 부수 효과로 메시지를 생성하고 전송할 수 있게 해줍니다.  
예를 들어, 어떤 컴포넌트가 있고, 이 컴포넌트의 상태가 변경될 때마다 메시지를 통해 알림을 받고 싶다고 가정해봅시다.  
이러한 알림 메시지를 전송하는 가장 쉬운 방법은 전용 채널로 메시지를 보내는 것이지만, 상태를 변경하는 메서드 호출과 메시지 전송 프로세스를 어떻게 연결해야 할까요? 또한 알림 메시지는 어떻게 구조화해야 할까요?  
Spring Integration의 AOP 메시지 발행 기능은 이러한 책임을 구성 기반(configuration-driven) 방식으로 처리합니다.

### 메시지 발행 구성 (Message Publishing Configuration)
Spring Integration은 두 가지 접근 방식을 제공합니다: 
- XML 구성 방식
- 애노테이션 기반(Java) 구성 방식입니다.

### `@Publisher` 애노테이션을 사용한 애노테이션 기반 구성
애노테이션 기반 접근 방식에서는 `@Publisher` 애노테이션을 사용하여 특정 메서드를 표시하고, channel 속성을 지정할 수 있습니다.  
5.1 버전부터 이 기능을 활성화하려면 `@Configuration` 클래스에 `@EnablePublisher`애노테이션을 추가해야 합니다.  
(Configuration and @EnableIntegration 참조)

메시지는 메서드 호출의 반환값으로부터 생성되며, channel 속성으로 지정된 채널로 전송됩니다. 메시지 구조를 더 세밀하게 관리하고 싶다면, `@Payload`와 `@Header` 애노테이션을 조합해서 사용할 수도 있습니다.

내부적으로, Spring Integration의 메시지 발행 기능은 Spring AOP(PublisherAnnotationAdvisor)와 Spring Expression Language(SpEL)를 함께 사용하여 메시지 구조를 매우 유연하게 제어할 수 있습니다.

`PublisherAnnotationAdvisor`가 정의하고 바인딩하는 변수:
- `#return`: 메서드 반환값에 바인딩되며, 반환 객체나 속성에 접근할 수 있습니다. (예: `#return.something`, something은 `#return` 객체의 속성)
- `#exception`: 메서드 호출 중 예외가 발생하면 예외 객체에 바인딩됩니다.
- `#args`: 메서드 인자에 바인딩되며, 이름으로 개별 인자를 추출할 수 있습니다. (예: `#args.fname`)

다음 예제를 참고하세요:
```
@Publisher
public String defaultPayload(String fname, String lname) {
  return fname + " " + lname;
}
```
앞의 예제에서, 메시지는 다음과 같은 구조로 생성됩니다:
- 메시지 페이로드(payload)는 메서드의 반환 타입과 값입니다. 이것이 기본 동작입니다.
- 새로 생성된 메시지는 애노테이션 후처리기(annotation post processor)로 구성된 기본 발행 채널(default publisher channel)로 전송됩니다(이후 섹션에서 자세히 다룸).

다음 예제는 앞의 예제와 동일하지만, 기본 발행 채널을 사용하지 않습니다:
```
@Publisher(channel="testChannel")
public String defaultPayload(String fname, @Header("last") String lname) {
  return fname + " " + lname;
}
```
기본 발행 채널을 사용하는 대신, `@Publisher` 애노테이션의 `channel` 속성을 설정하여 발행 채널을 지정합니다.  
또한 `@Header` 애노테이션을 추가하여, 메시지 헤더 중 'last'가 메서드 매개변수 'lname'과 동일한 값을 갖도록 합니다.  
이 헤더는 새로 생성된 메시지에 추가됩니다.

다음 예제는 앞선 예제와 거의 동일합니다:
```
@Publisher(channel="testChannel")
@Payload
public String defaultPayloadButExplicitAnnotation(String fname, @Header String lname) {
  return fname + " " + lname;
}
```
유일한 차이점은 메서드에 `@Payload` 애노테이션을 사용하여, 메서드의 반환값이 메시지의 페이로드로 사용되어야 함을 명시적으로 지정했다는 점입니다.

다음 예제는 `@Payload` 애노테이션에서 Spring Expression Language(SpEL)를 사용하여, 메시지가 어떻게 구성되어야 하는지 프레임워크에 추가로 지시하는 방식으로 이전 구성 예제를 확장한 것입니다:
```
@Publisher(channel="testChannel")
@Payload("#return + #args.lname")
public String setName(String fname, String lname, @Header("x") int num) {
  return fname + " " + lname;
}
```
앞선 예제에서, 메시지는 메서드 호출의 반환값과 'lname' 입력 인자의 값을 연결(concatenation)한 결과입니다.  
'x'라는 이름의 메시지 헤더는 'num' 입력 인자의 값으로 설정되며, 이 헤더가 새로 생성된 메시지에 추가됩니다.

```
@Publisher(channel="testChannel")
public String argumentAsPayload(@Payload String fname, @Header String lname) {
  return fname + " " + lname;
}
```
앞선 예제에서는 `@Payload` 애너테이션의 또 다른 사용 예를 볼 수 있습니다.  
여기서는 **메서드의 인자**를 애너테이션 처리하여 새로 생성되는 메시지의 페이로드로 사용합니다.

Spring의 대부분의 애너테이션 기반 기능과 마찬가지로, `PublisherAnnotationBeanPostProcessor`라는 포스트 프로세서를 등록해야 합니다.  
다음 예제는 이를 수행하는 방법을 보여줍니다:
```
<bean class="org.springframework.integration.aop.PublisherAnnotationBeanPostProcessor"/>
```

보다 간결한 설정을 위해, 다음 예제에서 보듯 네임스페이스 지원을 사용할 수도 있습니다:
```
<int:annotation-config>
    <int:enable-publisher default-publisher-channel="defaultChannel"/>
</int:annotation-config>
```
Java 설정에서는, 다음 예제에서 보듯이 `@EnablePublisher` 애노테이션을 사용해야 합니다:
```
@Configuration
@EnableIntegration
@EnablePublisher("defaultChannel")
public class IntegrationConfiguration {
    ...
}
```
버전 5.1.3부터 `<int:enable-publisher>` 컴포넌트와 `@EnablePublisher` 애노테이션은 ProxyFactory 설정을 조정하기 위해 `proxy-target-class`와 `order` 속성을 제공합니다.

다른 Spring 애노테이션(`@Component`, `@Scheduled` 등)과 마찬가지로, `@Publisher`를 [메타 애노테이션](../../terminology/post/m.md#메타-애노테이션meta-annotation)으로 사용할 수도 있습니다.  
이는 `@Publisher` 자체와 동일하게 처리되는 사용자 정의 애노테이션을 정의할 수 있다는 의미입니다.  
다음 예제가 그 방법을 보여줍니다:
```
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Publisher(channel="auditChannel")
public @interface Audit { ... }
```
앞선 예제에서는 `@Publisher`로 주석 처리된 `@Audit` 애노테이션을 정의했습니다.  
또한, 메타 애노테이션에 `channel` 속성을 정의하여 메시지가 이 애노테이션 내부에서 어디로 전송될지를 캡슐화할 수 있습니다.  
이제 다음 예제와 같이 @Audit 애노테이션을 어떤 메서드에든 적용할 수 있습니다:
```
@Audit
public String test() {
    return "Hello";
}
```
앞선 예제에서, test() 메서드가 호출될 때마다 그 반환 값으로 생성된 페이로드를 가진 메시지가 생성됩니다.  
각 메시지는 `auditChannel`이라는 이름의 채널로 전송됩니다.  
이 기법의 장점 중 하나는 동일한 채널 이름을 여러 애노테이션에서 중복해서 정의하는 것을 피할 수 있다는 점입니다.  
또한, 도메인 특화 애노테이션과 프레임워크에서 제공하는 애노테이션 사이에 간접적인 연결 계층을 제공할 수 있습니다.

**클래스에 애노테이션을 적용**할 수도 있는데, 이렇게 하면 해당 클래스의 모든 public 메서드에 이 애노테이션의 속성이 적용됩니다.  
다음 예제가 이를 보여줍니다:
```
@Audit
static class BankingOperationsImpl implements BankingOperations {
  public String debit(String amount) { . . . }
  public String credit(String amount) { . . . }
}
```

### XML 기반 접근 방식: <publishing-interceptor> 요소
XML 기반 접근 방식은 애노테이션 기반 대신 `MessagePublishingInterceptor`를 네임스페이스 기반으로 구성하여 동일한 AOP 기반 메시지 발행 기능을 설정할 수 있습니다.  
이 방식은 애노테이션 기반 접근 방식에 비해 몇 가지 장점이 있는데, **AOP 포인트컷 표현식**을 사용할 수 있어 한 번에 여러 메서드를 가로채거나, 소스 코드를 직접 갖고 있지 않은 메서드도 가로채어 메시지를 발행할 수 있다는 점입니다.

XML로 메시지 발행을 구성하려면 다음 두 가지를 수행하면 됩니다:
- `<publishing-interceptor>` XML 요소를 사용하여 `MessagePublishingInterceptor`를 구성합니다.
- AOP 구성을 제공하여 `MessagePublishingInterceptor`를 관리되는 객체에 적용합니다.

다음 예제는 `publishing-interceptor` 요소를 구성하는 방법을 보여줍니다:
```
<aop:config>
  <aop:advisor advice-ref="interceptor" pointcut="bean(testBean)" />
</aop:config>
<publishing-interceptor id="interceptor" default-channel="defaultChannel">
  <method pattern="echo" payload="'Echoing: ' + #return" channel="echoChannel">
    <header name="things" value="something"/>
  </method>
  <method pattern="repl*" payload="'Echoing: ' + #return" channel="echoChannel">
    <header name="things" expression="'something'.toUpperCase()"/>
  </method>
  <method pattern="echoDef*" payload="#return"/>
</publishing-interceptor>
```
`<publishing-interceptor>` 구성은 애노테이션 기반 접근 방식과 매우 유사하며, Spring Expression Language의 기능도 활용합니다.  
앞선 예제에서, `testBean`의 `echo` 메서드 실행은 다음과 같은 구조의 메시지를 생성합니다:
- `메시지` 페이로드(payload)는 `String` 타입이며 내용은 `Echoing: [value]`입니다. 여기서 value는 실행된 메서드가 반환한 값입니다.
- `메시지`에는 이름이 `things`이고 값이 `something`인 헤더가 포함됩니다.
- `메시지`는 `echoChannel`로 전송됩니다.

두 번째 메서드도 첫 번째와 매우 유사합니다. 여기서는 이름이 `repl로 시작하는 모든 메서드 실행`이 다음 구조의 메시지를 생성합니다:
- 메시지 페이로드는 이전 샘플과 동일합니다.
- 메시지에는 things라는 이름의 헤더가 있으며, 값은 SpEL 표현식 'something'.toUpperCase()의 결과입니다.
- 메시지는 echoChannel로 전송됩니다.

세 번째 메서드는 `echoDef로 시작하는 모든 메서드 실행`을 매핑하며, 다음 구조의 메시지를 생성합니다:
- 메시지 페이로드는 실행된 메서드가 반환한 값입니다.
- channel 속성이 제공되지 않았으므로, 메시지는 퍼블리셔에 정의된 defaultChannel로 전송됩니다.

간단한 매핑 규칙의 경우, 퍼블리셔의 기본값을 활용할 수 있습니다:
```
<publishing-interceptor id="anotherInterceptor"/>
```
앞선 예제에서는, 포인트컷(pointcut) 표현식과 일치하는 모든 메서드의 반환값을 페이로드로 매핑하여 default-channel로 전송합니다.  
만약 defaultChannel을 지정하지 않으면(앞선 예제처럼), 메시지는 글로벌 nullChannel로 전송됩니다. (/dev/null과 동일한 역할을 함)

### 비동기(Asynchronous) 퍼블리싱
퍼블리싱은 기본적으로 컴포넌트 실행과 동일한 스레드에서 수행되므로, 기본적으로 동기식입니다.  
즉, 메시지 퍼블리셔의 흐름이 완료될 때까지 전체 메시지 흐름이 대기합니다.  
그러나 개발자들은 종종 정반대의 동작, 즉 비동기 흐름을 시작하기 위해 이 메시지 퍼블리싱 기능을 사용하고 싶어합니다.  
예를 들어, HTTP나 WS 같은 서비스를 호스팅하며 원격 요청을 받을 경우, 이 요청을 내부적으로 처리하는 데 시간이 걸릴 수 있습니다.  
하지만 사용자는 즉시 응답을 받기를 원할 수 있습니다.

이때, 전통적인 방식처럼 인바운드 요청을 바로 출력 채널로 보내는 대신, output-channel 속성이나 replyChannel 헤더를 사용하여 호출자에게 간단한 확인 메시지를 바로 보내고, 동시에 메시지 퍼블리셔 기능을 이용해 복잡한 흐름을 비동기적으로 시작할 수 있습니다.

다음 예제에서는, 서비스가 복잡한 페이로드를 받아 추가 처리로 전달해야 하지만, 호출자에게는 단순 확인 메시지를 즉시 응답하는 상황을 보여줍니다:
```
public String echo(Object complexPayload) {
     return "ACK";
}
```
즉, 복잡한 흐름을 출력 채널에 연결하는 대신, 메시지 퍼블리싱 기능을 사용합니다.  
서비스 메서드의 입력 인수를 사용하여 새 메시지를 생성하고(앞선 예제 참조) 이를 'localProcessChannel'로 전송하도록 구성합니다.

이 흐름을 비동기로 만들기 위해서는, 단순히 이 메시지를 비동기 채널(다음 예제에서는 `ExecutorChannel`)로 보내면 됩니다.
다음 예제는 **비동기 퍼블리싱 인터셉터(asynchronous publishing-interceptor)**를 구성하는 방법을 보여줍니다:
```
<int:service-activator  input-channel="inputChannel" output-channel="outputChannel" ref="sampleservice"/>

<bean id="sampleService" class="test.SampleService"/>

<aop:config>
  <aop:advisor advice-ref="interceptor" pointcut="bean(sampleService)" />
</aop:config>

<int:publishing-interceptor id="interceptor" >
  <int:method pattern="echo" payload="#args[0]" channel="localProcessChannel">
    <int:header name="sample_header" expression="'some sample value'"/>
  </int:method>
</int:publishing-interceptor>

<int:channel id="localProcessChannel">
  <int:dispatcher task-executor="executor"/>
</int:channel>

<task:executor id="executor" pool-size="5"/>
```
이러한 시나리오를 처리하는 또 다른 방법은 **와이어탭(wire-tap)**을 사용하는 것입니다. 자세한 내용은 Wire Tap를 참조하세요.

### 스케줄 트리거에 기반한 메시지 생성 및 퍼블리싱
앞선 섹션에서는 메서드 호출의 부산물로 메시지를 생성하고 퍼블리싱하는 기능을 살펴보았습니다.  
그러나 이러한 경우에는 여전히 메서드를 직접 호출해야 합니다.

Spring Integration 2.0에서는 스케줄링된 메시지 생성자 및 퍼블리셔를 지원하며, `<inbound-channel-adapter>` 요소의 expression 속성을 통해 구현됩니다.  
메시지 스케줄링은 여러 트리거를 기반으로 설정할 수 있으며, 각 트리거는 <`poller>` 요소에서 구성할 수 있습니다.  
현재 지원되는 트리거 유형은 `cron`, `fixed-rate`, `fixed-delay`, 그리고 사용자가 직접 구현하여 trigger 속성으로 참조하는 커스텀 트리거입니다.

앞서 언급했듯, 스케줄링된 메시지 생성자 및 퍼블리셔 지원은 `<inbound-channel-adapter>` XML 요소를 통해 제공됩니다:
```
<int:inbound-channel-adapter id="fixedDelayProducer"
       expression="'fixedDelayTest'"
       channel="fixedDelayChannel">
    <int:poller fixed-delay="1000"/>
</int:inbound-channel-adapter>
```
앞선 예제에서는 inbound channel adapter를 생성하여, expression 속성에 정의된 식의 결과를 페이로드로 갖는 메시지를 구성합니다.  
이러한 메시지는 `fixed-delay` 속성에 지정된 지연 시간마다 생성되어 전송됩니다.

다음 예제는 앞선 예제와 유사하지만, 이번에는 `fixed-rate` 속성을 사용합니다:
```
<int:inbound-channel-adapter id="fixedRateProducer"
       expression="'fixedRateTest'"
       channel="fixedRateChannel">
    <int:poller fixed-rate="1000"/>
</int:inbound-channel-adapter>
```
`fixed-rate` 속성은 각 작업의 시작 시점을 기준으로 일정한 간격으로 메시지를 전송할 수 있게 해줍니다.

다음 예제는 `cron` 속성에 지정된 값을 사용하여 Cron 트리거를 적용하는 방법을 보여줍니다:
```
<int:inbound-channel-adapter id="cronProducer"
       expression="'cronTest'"
       channel="cronChannel">
    <int:poller cron="7 6 5 4 3 ?"/>
</int:inbound-channel-adapter>
```

다음 예제는 메시지에 추가 헤더를 삽입하는 방법을 보여줍니다:
```
<int:inbound-channel-adapter id="headerExpressionsProducer"
       expression="'headerExpressionsTest'"
       channel="headerExpressionsChannel"
       auto-startup="false">
    <int:poller fixed-delay="5000"/>
    <int:header name="foo" expression="6 * 7"/>
    <int:header name="bar" value="x"/>
</int:inbound-channel-adapter>
```
추가된 메시지 헤더는 스칼라 값이거나 Spring 표현식을 평가한 결과일 수 있습니다.

자체 커스텀 트리거를 구현해야 하는 경우, trigger 속성을 사용하여 org.springframework.scheduling.Trigger 인터페이스를 구현한 Spring에서 구성된 어떤 빈이든 참조할 수 있습니다:
```
<int:inbound-channel-adapter id="triggerRefProducer"
       expression="'triggerRefTest'" channel="triggerRefChannel">
    <int:poller trigger="customTrigger"/>
</int:inbound-channel-adapter>

<beans:bean id="customTrigger" class="o.s.scheduling.support.PeriodicTrigger">
    <beans:constructor-arg value="9999"/>
</beans:bean>
```