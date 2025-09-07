# Spring Integration
## [메시지 채널 구현체 (Message Channel Implementations)](https://docs.spring.io/spring-integration/reference/channel/implementations.html)

Spring Integration은 여러 가지 메시지 채널 구현체를 제공합니다. 다음 섹션에서는 각각을 간단히 설명합니다.

### PublishSubscribeChannel
`PublishSubscribeChannel` 구현체는 해당 채널로 전송된 모든 메시지를 구독 중인 모든 핸들러에게 브로드캐스트합니다.  
이 채널은 주로 **알림(Notification)**을 목적으로 하는 이벤트 메시지를 전송할 때 사용됩니다.  
(반대로, 문서(Document) 메시지는 일반적으로 단일 핸들러에 의해 처리되도록 의도됩니다.)

`PublishSubscribeChannel`은 오직 전송(Send) 용도로만 설계되었습니다.  
`send(Message)` 메서드가 호출되면, 메시지를 즉시 모든 구독자에게 브로드캐스트합니다.  
따라서 소비자가 메시지를 poll 방식으로 가져올 수 없습니다. (`PollableChannel`을 구현하지 않았기 때문에 `receive()` 메서드가 존재하지 않습니다.)  
따라서 구독자는 반드시 `MessageHandler`여야 하며, 구독자의 `handleMessage(Message)` 메서드가 차례로 호출됩니다.

버전 3.0 이전에는 `PublishSubscribeChannel`에 구독자가 없을 때 `send` 메서드를 호출하면 `false`를 반환했습니다.  
그리고 `MessagingTemplate`와 함께 사용할 경우 `MessageDeliveryException`이 발생했습니다.  
하지만 버전 3.0부터는 동작이 변경되어, 최소 구독자 수 이상이 존재하고 메시지를 성공적으로 처리하면 전송이 항상 성공으로 간주됩니다.  
이 동작은 `minSubscribers` 속성으로 변경할 수 있으며, 기본값은 0입니다.

>만약 `TaskExecutor`를 사용할 경우, 올바른 수의 구독자가 존재하는지만 판단합니다. 왜냐하면 실제 메시지 처리는 비동기적으로 수행되기 때문입니다.

### QueueChannel
`QueueChannel` 구현체는 **큐(Queue)**를 감쌉니다.  
`PublishSubscribeChannel`과는 달리, `QueueChannel`은 점대점(point-to-point) 방식의 의미론을 가집니다.  
즉, 해당 채널에 여러 소비자가 있더라도, 그 채널로 전송된 하나의 메시지는 단 하나의 소비자만 수신할 수 있습니다.

`QueueChannel`은 **기본 생성자(매개변수 없음)**를 제공하는데, 이 경우 내부 큐의 용량(capacity)은 사실상 제한이 없는 Integer.MAX_VALUE 값으로 설정됩니다.  
또한, 큐의 용량을 지정할 수 있는 생성자도 제공하며, 아래 예시에서 확인할 수 있습니다:
```
public QueueChannel(int capacity)
```
채널이 아직 용량(capacity) 한도에 도달하지 않은 경우, 메시지는 내부 큐에 저장되고, `send(Message<?>)` 메서드는 수신자가 메시지를 처리할 준비가 되어 있지 않더라도 즉시 반환됩니다.  
반면 큐가 용량 한도에 도달한 경우, 송신자는 큐에 공간이 생길 때까지(block) 대기합니다.  
대안으로, 추가적인 timeout 파라미터가 있는 send 메서드를 사용하면, 큐는 공간이 생기거나 지정한 `timeout` 시간이 만료될 때까지 블로킹됩니다.

마찬가지로, `receive()` 호출은 큐에 메시지가 있으면 즉시 반환되지만, 큐가 비어 있으면 메시지가 들어오거나 timeout(지정된 경우)이 만료될 때까지 블로킹됩니다.  
양쪽 모두, timeout 값으로 0을 전달하면 큐의 상태와 상관없이 즉시 반환됩니다.  
단, timeout 파라미터가 없는 `send()` 및 `receive()` 메서드 호출은 무기한 블로킹된다는 점을 주의해야 합니다.

### PriorityChannel
`QueueChannel`이 `FIFO(선입선출)` 순서를 강제하는 반면, `PriorityChannel`은 메시지를 우선순위에 따라 정렬하여 처리할 수 있는 대안 구현체입니다.  
기본적으로 우선순위는 메시지의 `priority` 헤더 값에 의해 결정됩니다.  
그러나 사용자 정의 우선순위 로직이 필요하다면, `PriorityChannel` 생성자에 **`Comparator<Message<?>>` 타입의 비교자(comparator)**를 제공할 수도 있습니다.

### RendezvousChannel
`RendezvousChannel`은 “직접 핸드오프(direct-handoff)” 시나리오를 가능하게 합니다.  
즉, 송신자는 다른 쪽에서 `receive()` 메서드를 호출할 때까지 블로킹되고, 반대로 수신자는 송신자가 메시지를 보낼 때까지 블로킹됩니다.

내부적으로는 `QueueChannel`과 유사하지만, **용량(capacity)이 0인 SynchronousQueue**를 사용한다는 점이 다릅니다.  
이는 송신자와 수신자가 서로 다른 스레드에서 동작하지만, 메시지를 큐에 비동기적으로 쌓는 방식이 적절하지 않은 상황에서 잘 동작합니다.  
즉, `RendezvousChannel`에서는 송신자는 수신자가 메시지를 실제로 수신했다는 것을 보장받을 수 있지만, `QueueChannel`에서는 메시지가 내부 큐에만 저장되고 실제로 수신되지 않을 가능성도 있습니다.

>이러한 큐 기반 채널들은 기본적으로 메시지를 메모리 내에서만 저장합니다.  
>만약 **영속성(persistence)**이 필요하다면, <queue> 요소의 'message-store' 속성으로 Persistent MessageStore 구현체를 참조하거나, 로컬 채널을 JMS 기반 채널 또는 채널 어댑터로 교체할 수 있습니다.  
>후자의 방법을 사용하면, JMS 공급자의 메시지 영속성 기능을 활용할 수 있습니다(👉 JMS 지원 부분 참고).  
>하지만 큐 버퍼링이 필요하지 않은 경우, 가장 단순한 접근법은 DirectChannel을 사용하는 것입니다(다음 섹션에서 다룸).  

또한, `RendezvousChannel`은 요청-응답(request-reply) 연산을 구현할 때 유용합니다.
예를 들어 송신자가 임시 익명 `RendezvousChannel` 인스턴스를 생성한 뒤, 메시지를 만들 때 이를 `replyChannel` 헤더로 설정할 수 있습니다.
그 후 메시지를 전송하고, 즉시 `receive()`(선택적으로 timeout 지정 가능)를 호출하여 응답 메시지를 기다리며 블로킹할 수 있습니다.
이 방식은 Spring Integration의 많은 요청-응답 컴포넌트들이 내부적으로 사용하는 구현 방식과 매우 유사합니다.

### DirectChannel
`DirectChannel`은 점대점(point-to-point) 방식의 의미론을 가지지만, 그 외의 동작은 앞에서 설명한 큐 기반 채널 구현체보다는 `PublishSubscribeChannel`과 더 유사합니다.  
`PollableChannel` 인터페이스 대신 `SubscribableChannel` 인터페이스를 구현하기 때문에, 메시지를 구독자에게 직접 전달합니다.  
그러나 점대점 채널이므로, `PublishSubscribeChannel`과 달리 각 메시지는 단 하나의 구독된 `MessageHandler`에게만 전송됩니다.

가장 단순한 점대점 채널 옵션일 뿐만 아니라, `DirectChannel`의 중요한 특징 중 하나는 채널 양쪽 작업을 단일 스레드가 처리할 수 있다는 점입니다.  
예를 들어, 어떤 핸들러가 `DirectChannel`을 구독하고 있다면, 이 채널로 메시지를 전송하는 순간 그 핸들러의 `handleMessage(Message)` 메서드가 보내는 쪽 스레드 안에서 직접 호출됩니다.  
이 동작은 `send()` 메서드 호출이 반환되기 전에 일어납니다.

이런 동작 방식을 제공하는 핵심 동기는, 채널을 통한 추상화와 느슨한 결합(loose coupling)의 이점을 유지하면서도, 트랜잭션을 채널을 가로질러(span across) 보장하기 위함입니다.  
만약 `send()` 호출이 트랜잭션 범위 내에서 실행된다면, 핸들러 호출의 결과(예: DB 레코드 갱신)는 해당 트랜잭션의 최종 결과(커밋 또는 롤백)에 영향을 미치게 됩니다.

>`DirectChannel`은 가장 단순한 옵션이고, 폴러(poller)의 스레드 관리와 스케줄링에 필요한 추가 오버헤드가 없으므로 Spring Integration에서 기본 채널 타입으로 사용됩니다.  
>일반적인 설계 방식은 애플리케이션에 필요한 채널들을 정의한 뒤, 버퍼링이나 입력 조절(throttling) 이 필요한 경우에만 큐 기반 `PollableChannel`로 바꾸는 것입니다.  
>또한 메시지를 브로드캐스트해야 하는 경우에는 `DirectChannel`이 아닌 `PublishSubscribeChannel`을 사용해야 합니다.  
>이후에 각 채널을 설정하는 방법을 예제로 보여줍니다.

`DirectChannel`은 내부적으로 메시지 디스패처에 위임하여 구독된 메시지 핸들러를 호출하며, 이 디스패처는 `load-balancer` 또는 `load-balancer-ref` 속성(상호 배타적)을 통해 로드 밸런싱 전략을 가질 수 있습니다.  
로드 밸런싱 전략은 여러 메시지 핸들러가 동일한 채널을 구독할 때 메시지가 어떻게 분배될지 결정하는 데 사용됩니다.  
편의를 위해 `load-balancer` 속성은 기존의 `LoadBalancingStrategy` 구현을 가리키는 열거형 값을 제공합니다.  
사용 가능한 값은 `라운드로빈(핸들러 간 순환 로드 밸런싱)`과 `none(로드 밸런싱을 명시적으로 비활성화하고자 할 때)`뿐입니다.  
향후 버전에서는 다른 전략 구현이 추가될 수 있습니다.  
그러나 버전 3.0 이후에는 `LoadBalancingStrategy`를 직접 구현하고 `load-balancer-ref` 속성을 사용하여 주입할 수 있으며, 이 속성은 `LoadBalancingStrategy`를 구현한 빈을 가리켜야 합니다.

`FixedSubscriberChannel`은 단일 `MessageHandler` 구독자만 지원하며, 구독을 취소할 수 없는 `SubscribableChannel`입니다.  
이는 다른 구독자가 없고 채널 인터셉터가 필요 없는 고처리량 성능 요구 사항에 유용합니다.
```
<int:channel id="lbRefChannel">
  <int:dispatcher load-balancer-ref="lb"/>
</int:channel>

<bean id="lb" class="foo.bar.SampleLoadBalancingStrategy"/>
```
`load-balancer`와 `load-balancer-ref` 속성은 서로 동시에 사용할 수 없습니다.

로드 밸런싱은 불리언 속성인 `failover`와 함께 동작합니다.  
`failover` 값이 true(기본값)일 경우, 디스패처는 앞선 핸들러가 예외를 던지면 필요에 따라 다음 핸들러로 넘어가며 실행합니다.  
실행 순서는 핸들러에 설정된 `order` 값(선택 사항)에 따라 결정되며, 값이 없다면 구독한 순서대로 실행됩니다.

만약 어떤 상황에서 디스패처가 항상 첫 번째 핸들러를 먼저 실행하고, 오류 발생 시 동일한 순서대로 차례로 `fallback` 하길 원한다면, 로드 밸런싱 전략을 제공하지 않아야 합니다.  
즉, 로드 밸런싱이 활성화되지 않아도 `failover` 속성은 여전히 지원됩니다. 단, 이 경우 핸들러 실행은 항상 첫 번째부터 순서대로 시작됩니다.  
예를 들어 1차(primary), 2차(secondary), 3차(tertiary) 와 같이 명확히 우선순위가 정의된 경우 이 접근 방식이 적합합니다.  
네임스페이스 설정을 사용할 때는 각 엔드포인트의 `order` 속성이 실행 순서를 결정합니다.

>로드 밸런싱과 `failover`는 채널에 둘 이상의 메시지 핸들러가 구독된 경우에만 적용됩니다.  
>네임스페이스 설정을 사용할 경우, 이는 여러 엔드포인트가 동일한 `input-channel` 속성으로 정의된 채널을 공유하는 상황을 의미합니다.

버전 5.2 이상에서는 failover=true일 때, 현재 핸들러 실패와 함께 실패한 메시지가 debug 또는 info 레벨(환경설정에 따라)에 로그로 기록됩니다.

### ExecutorChannel
`ExecutorChannel`은 점대점(point-to-point) 채널이며, `DirectChannel`과 동일하게 디스패처 설정(로드 밸런싱 전략과 failover 속성)을 지원합니다.  
이 두 디스패칭 채널의 핵심 차이는, `ExecutorChannel`이 메시지 디스패치를 `TaskExecutor` 인스턴스에 위임한다는 점입니다.

즉, `send` 메서드는 일반적으로 블로킹되지 않으며, 메시지 핸들러 실행은 보내는 쪽 스레드가 아닌 별도의 스레드에서 수행됩니다.  
따라서 송신자와 수신 핸들러 간의 트랜잭션을 보장하지 않습니다.

>송신자가 블로킹될 수도 있는데, 이는 예를 들어 `ThreadPoolExecutor.CallerRunsPolicy`와 같이 클라이언트를 제어(throttling)하는 거부 정책을 가진 `TaskExecutor`를 사용할 때 발생합니다.  
>이 경우 스레드 풀이 최대 용량에 도달하고 실행 대기열이 가득 차면, 송신자의 스레드가 직접 메서드를 실행하게 됩니다.  
>하지만 이는 예측 불가능하게 발생하므로 트랜잭션 보장에 의존해서는 안 됩니다.

### PartitionedChannel
버전 6.1부터 `PartitionedChannel` 구현이 제공됩니다.  
이는 `AbstractExecutorChannel`의 확장이며, 실제 메시지 소비가 채널로 전송된 메시지에서 평가된 파티션 키에 따라 특정 스레드에서 처리되는 포인트-투-포인트(dispatch) 로직을 나타냅니다.  
이 채널은 위에서 언급한 `ExecutorChannel`과 유사하지만, 동일한 파티션 키를 가진 메시지는 항상 동일한 스레드에서 처리되어 순서를 유지한다는 점이 다릅니다.  
외부 `TaskExecutor`가 필요하지 않지만, 커스텀 `ThreadFactory(e.g. Thread.ofVirtual().name("partition-", 0).factory())`로 구성할 수 있습니다.  
이 `Factory`는 각 파티션마다 `MessageDispatcher` 대리자에 단일 스레드 실행기를 채우는 데 사용됩니다.  
기본적으로 `PartitionedChannel`은 `IntegrationMessageHeaderAccessor.CORRELATION_ID` 메시지 헤더를 파티션 키로 사용합니다.  
이 채널은 간단한 빈으로 구성할 수 있습니다.
```
@Bean
PartitionedChannel somePartitionedChannel() {
    return new PartitionedChannel(3, (message) -> message.getHeaders().get("partitionKey"));
}
```
채널은 **3개의 파티션(전용 스레드)**을 가지며, `partitionKey` 헤더를 사용해 메시지가 어떤 파티션에서 처리될지를 결정합니다.  
더 자세한 정보는 PartitionedChannel 클래스의 Javadoc을 참고하세요.

### FluxMessageChannel
`FluxMessageChannel`은 `org.reactivestreams.Publisher` 구현체로, 전송된 메시지를 내부의 `reactor.core.publisher.Flux`로 "sink"하여 하류의 리액티브 구독자가 필요할 때 소비할 수 있도록 합니다.  
이 채널 구현체는 `SubscribableChannel`도 아니고 `PollableChannel`도 아니므로, 오직 `org.reactivestreams.Subscriber` 인스턴스만이 리액티브 스트림의 [백프레셔(back-pressure)](../../terminology/post/b.md#백프레셔back-pressure) 특성을 존중하며 이 채널에서 메시지를 소비할 수 있습니다.  
한편, `FluxMessageChannel`은 `subscribeTo(Publisher<Message<?>>)` 계약을 가진 `ReactiveStreamsSubscribableChannel`을 구현하여, 리액티브 소스 퍼블리셔로부터 이벤트를 수신하고 이를 통합 흐름(integration flow)으로 브리지할 수 있습니다.  
전체 통합 흐름에서 완전히 리액티브 동작을 달성하려면, 이러한 채널을 흐름 내 모든 엔드포인트 사이에 배치해야 합니다.

리액티브 스트림과의 상호작용에 대한 자세한 내용은 Reactive Streams Support를 참조하세요.

### Scoped Channel
Spring Integration 1.0에서는 `ThreadLocalChannel` 구현체를 제공했지만, 2.0부터는 제거되었습니다.  
이제 동일한 요구 사항을 처리하는 보다 일반적인 방법은 채널에 `scope` 속성을 추가하는 것입니다.  
속성의 값은 컨텍스트 내에서 사용 가능한 스코프 이름이 될 수 있습니다.  
예를 들어, 웹 환경에서는 특정 스코프가 제공되며, 커스텀 스코프 구현체도 컨텍스트에 등록할 수 있습니다.  
다음 예시는 채널에 스레드 로컬(thread-local) 스코프를 적용하고, 스코프 자체를 등록하는 방법을 보여줍니다:
```
<int:channel id="threadScopedChannel" scope="thread">
     <int:queue />
</int:channel>

<bean class="org.springframework.beans.factory.config.CustomScopeConfigurer">
    <property name="scopes">
        <map>
            <entry key="thread" value="org.springframework.context.support.SimpleThreadScope" />
        </map>
    </property>
</bean>
```
이전 예제에서 정의된 채널은 내부적으로 큐(queue)에 위임(delegate)되지만, 채널이 현재 스레드에 바인딩되어 있으므로 큐의 내용물도 동일하게 바인딩됩니다.  
이렇게 하면, 채널로 메시지를 보내는 스레드는 이후에 동일한 메시지를 받을 수 있지만, 다른 스레드는 접근할 수 없습니다.  
스레드 범위(thread-scoped) 채널은 거의 필요하지 않지만, DirectChannel 인스턴스를 사용하여 단일 스레드에서 동작을 강제해야 하고, 모든 응답 메시지를 “터미널” 채널로 보내야 하는 경우에 유용할 수 있습니다.  
만약 그 터미널 채널이 스레드 범위라면, 원래 메시지를 보낸 스레드는 터미널 채널에서 응답을 수집할 수 있습니다.

이제 모든 채널이 스코프를 가질 수 있으므로, 스레드 로컬 외에도 원하는 스코프를 정의할 수 있습니다.