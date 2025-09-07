메시지 게시
(관점 지향 프로그래밍) AOP 메시지 게시 기능을 사용하면 메서드 호출의 부산물로 메시지를 생성하고 전송할 수 있습니다. 예를 들어, 컴포넌트가 있고 이 컴포넌트의 상태가 변경될 때마다 메시지를 통해 알림을 받고 싶다고 가정해 보겠습니다. 이러한 알림을 전송하는 가장 쉬운 방법은 전용 채널로 메시지를 전송하는 것입니다. 하지만 객체의 상태를 변경하는 메서드 호출을 메시지 전송 프로세스에 어떻게 연결해야 하며, 알림 메시지는 어떻게 구성해야 할까요? AOP 메시지 게시 기능은 구성 기반 접근 방식을 통해 이러한 역할을 처리합니다.

메시지 게시 구성
Spring Integration은 XML 구성과 주석 기반(Java) 구성의 두 가지 접근 방식을 제공합니다.

주석 기반 구성 및 @Publisher주석
어노테이션 기반 접근 방식을 사용하면 모든 메서드에 @Publisher'channel' 속성을 지정하는 어노테이션을 추가할 수 있습니다. 버전 5.1부터 이 기능을 활성화하려면 @EnablePublisher특정 @Configuration클래스에서 어노테이션을 사용해야 합니다. 자세한 내용은 구성 및 을@EnableIntegration@Payload 참조하십시오. 메시지는 메서드 호출의 반환값으로 생성되어 'channel' 속성으로 지정된 채널로 전송됩니다. 메시지 구조를 더욱 세부적으로 관리하기 위해 어노테이션 과 어노테이션을 함께 사용할 수도 있습니다 @Header.

내부적으로 Spring Integration의 이 메시지 게시 기능은 Spring AOP 정의 PublisherAnnotationAdvisor와 Spring 표현 언어(SpEL)를 모두 사용하여 게시되는 메시지의 구조에 대해 상당한 유연성과 제어력을 제공합니다 Message.

PublisherAnnotationAdvisor다음 변수를 정의하고 바인딩합니다 .

#return: 반환 값에 바인딩하여 해당 값이나 해당 속성을 참조할 수 있도록 합니다(예: #return.something'뭔가'가 바인딩된 개체의 속성인 경우 #return)

#exception: 메서드 호출로 인해 예외가 발생하면 해당 예외에 바인딩합니다.

#args: 메서드 인수에 바인딩하여 이름으로 개별 인수를 추출할 수 있습니다(예: #args.fname)

다음 예를 살펴보세요.

@Publisher
public String defaultPayload(String fname, String lname) {
  return fname + " " + lname;
}
이전 예에서 메시지는 다음과 같은 구조로 구성됩니다.

메시지 페이로드는 메서드의 반환 유형과 값입니다. 이는 기본값입니다.

새로 구성된 메시지는 주석 후처리기(이 섹션의 뒷부분에서 설명)로 구성된 기본 게시자 채널로 전송됩니다.

다음 예제는 기본 게시 채널을 사용하지 않는다는 점을 제외하면 앞의 예제와 동일합니다.

@Publisher(channel="testChannel")
public String defaultPayload(String fname, @Header("last") String lname) {
  return fname + " " + lname;
}
기본 게시 채널을 사용하는 대신, 애노테이션의 'channel' 속성을 설정하여 게시 채널을 지정합니다 @Publisher. 또한 애노테이션을 추가하면 @Header'last'라는 이름의 메시지 헤더가 'lname' 메서드 매개변수와 동일한 값을 갖게 됩니다. 이 헤더는 새로 생성된 메시지에 추가됩니다.

다음 예는 앞의 예와 거의 동일합니다.

@Publisher(channel="testChannel")
@Payload
public String defaultPayloadButExplicitAnnotation(String fname, @Header String lname) {
  return fname + " " + lname;
}
유일한 차이점은 @Payload메서드에 주석을 사용하여 메서드의 반환 값을 메시지의 페이로드로 사용해야 한다는 것을 명시적으로 지정한다는 것입니다.

다음 예제에서는 @Payload주석에서 Spring 표현 언어를 사용하여 이전 구성을 확장하여 프레임워크에 메시지를 구성하는 방법에 대한 추가 지침을 제공합니다.

@Publisher(channel="testChannel")
@Payload("#return + #args.lname")
public String setName(String fname, String lname, @Header("x") int num) {
  return fname + " " + lname;
}
앞의 예에서 메시지는 메서드 호출의 반환 값과 'lname' 입력 인수를 연결한 것입니다. 'x'라는 이름의 메시지 헤더는 'num' 입력 인수에 따라 값이 결정됩니다. 이 헤더는 새로 생성된 메시지에 추가됩니다.

@Publisher(channel="testChannel")
public String argumentAsPayload(@Payload String fname, @Header String lname) {
  return fname + " " + lname;
}
앞의 예제에서 @Payload어노테이션의 또 다른 사용법을 볼 수 있습니다. 여기서는 새로 생성된 메시지의 페이로드가 되는 메서드 인수에 어노테이션을 추가합니다.

Spring의 다른 대부분의 어노테이션 기반 기능과 마찬가지로, 포스트 프로세서( PublisherAnnotationBeanPostProcessor)를 등록해야 합니다. 다음 예제는 그 방법을 보여줍니다.

<bean class="org.springframework.integration.aop.PublisherAnnotationBeanPostProcessor"/>
더 간결한 구성을 위해 다음 예제와 같이 네임스페이스 지원을 대신 사용할 수 있습니다.

<int:annotation-config>
    <int:enable-publisher default-publisher-channel="defaultChannel"/>
</int:annotation-config>
@EnablePublisherJava 구성의 경우 다음 예제와 같이 주석을 사용해야 합니다 .

@Configuration
@EnableIntegration
@EnablePublisher("defaultChannel")
public class IntegrationConfiguration {
    ...
}
버전 5.1.3부터 <int:enable-publisher>​​구성 요소와 주석에 구성을 조정하기 위한 및 속성이 @EnablePublisher추가되었습니다 .proxy-target-classorderProxyFactory

다른 Spring 애노테이션( @Component, @Scheduled등) 과 마찬가지로 @Publisher메타 애노테이션으로도 사용할 수 있습니다. 즉, 자체 애노테이션과 동일한 방식으로 처리되는 자체 애노테이션을 정의할 수 있습니다 @Publisher. 다음 예제는 그 방법을 보여줍니다.

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Publisher(channel="auditChannel")
public @interface Audit {
...
}
앞의 예제에서는 @Audit애노테이션을 정의하고, 애노테이션 자체에 로 애노테이션을 추가했습니다 @Publisher. 또한, 메타 애노테이션에 속성을 정의하여 channel이 애노테이션 내에서 메시지가 전송되는 위치를 캡슐화할 수 있습니다. 이제 다음 예제와 같이 모든 메서드에 이 애노테이션을 추가할 수 있습니다 @Audit.

@Audit
public String test() {
    return "Hello";
}
앞의 예제에서 메서드를 호출할 때마다 test()반환 값에서 생성된 페이로드를 포함하는 메시지가 생성됩니다. 각 메시지는 이름이 .인 채널로 전송됩니다 auditChannel. 이 기법의 장점 중 하나는 여러 애노테이션에서 동일한 채널 이름이 중복되는 것을 방지할 수 있다는 것입니다. 또한, 사용자 고유의, 잠재적으로 도메인에 특화된 애노테이션과 프레임워크에서 제공하는 애노테이션 간에 간접적인 연결을 제공할 수 있습니다.

다음 예제와 같이 클래스에 주석을 달면 해당 클래스의 모든 공개 메서드에 이 주석의 속성을 적용할 수 있습니다.

@Audit
static class BankingOperationsImpl implements BankingOperations {

  public String debit(String amount) {
     . . .

  }

  public String credit(String amount) {
     . . .
  }

}
<publishing-interceptor>요소 를 사용한 XML 기반 접근 방식
XML 기반 접근 방식을 사용하면 네임스페이스 기반 구성과 동일한 AOP 기반 메시지 게시 기능을 구성할 수 있습니다 MessagePublishingInterceptor. AOP 포인트컷 표현식을 사용할 수 있으므로 여러 메서드를 동시에 가로채거나 소스 코드가 없는 메서드를 가로채서 게시할 수 있으므로 어노테이션 기반 접근 방식보다 확실히 몇 가지 이점이 있습니다.

XML을 사용하여 메시지 게시를 구성하려면 다음 두 가지 작업만 수행하면 됩니다.

MessagePublishingInterceptorXML 요소를 사용하여 구성을 제공합니다 <publishing-interceptor>.

관리되는 객체에 AOP 구성을 적용합니다 MessagePublishingInterceptor.

다음 예제에서는 publishing-interceptor요소를 구성하는 방법을 보여줍니다.

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
구성 <publishing-interceptor>은 주석 기반 접근 방식과 매우 유사하며 Spring 표현 언어의 기능을 사용합니다.

앞의 예에서 echoa의 메서드 를 실행하면 testBeana는 Message다음과 같은 구조로 렌더링됩니다.

페이 로드는 다음 내용을 갖는 Message유형입니다 . 여기서 는 실행된 메서드에서 반환된 값입니다.StringEchoing: [value]value

에는 Message이름이 . things이고 값이 something. 인 헤더가 있습니다.

는 Message로 전송됩니다 echoChannel.

두 번째 방법은 첫 번째 방법과 매우 유사합니다. 여기서 'repl'로 시작하는 모든 방법은 Message다음과 같은 구조의 를 렌더링합니다.

탑재량은 Message이전 샘플과 동일합니다.

에는 SpEL 표현식의 결과를 값으로 Message갖는 헤더가 있습니다 .things'something'.toUpperCase()

는 Message로 전송됩니다 echoChannel.

두 번째 방법은 로 시작하는 모든 메서드의 실행을 매핑하여 다음 구조의 echoDef를 생성합니다 .Message

페이 Message로드는 실행된 메서드에서 반환되는 값입니다.

channel속성이 제공되지 않았 으므로 는 에 정의된 Message로 전송됩니다 .defaultChannelpublisher

publisher간단한 매핑 규칙의 경우 다음 예와 같이 기본값을 사용할 수 있습니다 .

<publishing-interceptor id="anotherInterceptor"/>
앞의 예제는 포인트컷 표현식과 일치하는 모든 메서드의 반환 값을 페이로드에 매핑하여 로 전송합니다 default-channel. 를 지정하지 않으면 defaultChannel(앞의 예제처럼), 메시지는 전역 nullChannel( 와 동일 /dev/null)으로 전송됩니다.

비동기 게시
게시는 구성 요소 실행과 동일한 스레드에서 발생합니다. 따라서 기본적으로 동기식입니다. 즉, 게시자의 흐름이 완료될 때까지 전체 메시지 흐름이 대기해야 합니다. 그러나 개발자는 종종 정반대의 방식, 즉 이 메시지 게시 기능을 사용하여 비동기 흐름을 시작하기를 원합니다. 예를 들어, 원격 요청을 수신하는 서비스(HTTP, WS 등)를 호스팅한다고 가정해 보겠습니다. 이 요청을 시간이 걸릴 수 있는 프로세스로 내부적으로 전송하고 싶을 수 있습니다. 하지만 사용자에게 즉시 응답하고 싶을 수도 있습니다. 따라서 처리를 위해 인바운드 요청을 출력 채널로 전송하는 기존 방식 대신, 'output-channel' 또는 'replyChannel' 헤더를 사용하여 호출자에게 간단한 확인 응답과 유사한 응답을 보내고, 메시지 게시자 기능을 사용하여 복잡한 흐름을 시작할 수 있습니다.

다음 예제의 서비스는 복잡한 페이로드(처리를 위해 추가로 전송해야 함)를 수신하지만, 호출자에게 간단한 확인 응답도 해야 합니다.

public String echo(Object complexPayload) {
     return "ACK";
}
따라서 복잡한 흐름을 출력 채널에 연결하는 대신 메시지 게시 기능을 사용합니다. 앞의 예제에서처럼 서비스 메서드의 입력 인수를 사용하여 새 메시지를 생성하고 이를 'localProcessChannel'로 전송하도록 설정합니다. 이 흐름이 비동기적으로 동작하도록 하려면, 다음 예제에서처럼 모든 유형의 비동기 채널로 전송하기만 하면 됩니다 ExecutorChannel. 다음 예제는 비동기 방식을 사용하는 방법을 보여줍니다 publishing-interceptor.

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
이런 상황을 처리하는 또 다른 방법은 도청입니다. 도청을 참조하세요 .

예약된 트리거를 기반으로 메시지 생성 및 게시
이전 섹션에서는 메서드 호출의 부산물로 메시지를 생성하고 게시하는 메시지 게시 기능을 살펴보았습니다. 하지만 이러한 경우에도 메서드 호출은 여전히 ​​개발자의 책임입니다. Spring Integration 2.0에서는 expression'inbound-channel-adapter' 요소에 새로운 속성을 추가하여 예약된 메시지 생성자 및 게시자에 대한 지원을 추가했습니다. 여러 트리거를 기반으로 예약할 수 있으며, 그중 하나는 'poller' 요소에서 구성할 수 있습니다. 현재는 , cron, fixed-rate그리고 fixed-delay개발자가 구현하고 'trigger' 속성 값으로 참조하는 모든 사용자 지정 트리거를 지원합니다.

앞서 언급했듯이, 예약된 프로듀서와 퍼블리셔에 대한 지원은 <inbound-channel-adapter>XML 요소를 통해 제공됩니다. 다음 예를 살펴보겠습니다.

<int:inbound-channel-adapter id="fixedDelayProducer"
       expression="'fixedDelayTest'"
       channel="fixedDelayChannel">
    <int:poller fixed-delay="1000"/>
</int:inbound-channel-adapter>
Message앞의 예제는 속성 에 정의된 표현식의 결과를 페이로드로 갖는 를 생성하는 인바운드 채널 어댑터를 생성합니다 expression. 이러한 메시지는 속성에 지정된 지연 시간이 발생할 때마다 생성되어 전송됩니다 fixed-delay.

다음 예제는 fixed-rate속성을 사용한다는 점을 제외하면 앞의 예제와 비슷합니다.

<int:inbound-channel-adapter id="fixedRateProducer"
       expression="'fixedRateTest'"
       channel="fixedRateChannel">
    <int:poller fixed-rate="1000"/>
</int:inbound-channel-adapter>
이 fixed-rate속성을 사용하면 각 작업의 시작 시간을 기준으로 고정된 속도로 메시지를 보낼 수 있습니다.

다음 예제에서는 속성에 지정된 값으로 Cron 트리거를 적용하는 방법을 보여줍니다 cron.

<int:inbound-channel-adapter id="cronProducer"
       expression="'cronTest'"
       channel="cronChannel">
    <int:poller cron="7 6 5 4 3 ?"/>
</int:inbound-channel-adapter>
다음 예에서는 메시지에 추가 헤더를 삽입하는 방법을 보여줍니다.

<int:inbound-channel-adapter id="headerExpressionsProducer"
       expression="'headerExpressionsTest'"
       channel="headerExpressionsChannel"
       auto-startup="false">
    <int:poller fixed-delay="5000"/>
    <int:header name="foo" expression="6 * 7"/>
    <int:header name="bar" value="x"/>
</int:inbound-channel-adapter>
추가 메시지 헤더는 스칼라 값이나 Spring 표현식을 평가한 결과를 취할 수 있습니다.

사용자 지정 트리거를 구현해야 하는 경우, 해당 trigger속성을 사용하여 해당 인터페이스를 구현하는 Spring 구성 빈에 대한 참조를 제공할 수 있습니다 org.springframework.scheduling.Trigger. 다음 예제에서는 그 방법을 보여줍니다.

<int:inbound-channel-adapter id="triggerRefProducer"
       expression="'triggerRefTest'" channel="triggerRefChannel">
    <int:poller trigger="customTrigger"/>
</int:inbound-channel-adapter>

<beans:bean id="customTrigger" class="o.s.scheduling.support.PeriodicTrigger">
    <beans:constructor-arg value="9999"/>
</beans:bean>