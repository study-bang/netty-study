메시지 채널 구성(Configuring Message Channels)

메시지 채널 인스턴스를 생성하려면, XML에서는 <channel/> 요소를 사용하고, Java에서는 DirectChannel 인스턴스를 사용할 수 있습니다.
Java

XML
<int:channel id="exampleChannel"/>
@Bean
public MessageChannel exampleChannel() {
    return new DirectChannel();
}
<channel/> 요소를 하위 요소 없이 사용하면, 기본적으로 DirectChannel 인스턴스(SubscribableChannel)가 생성됩니다.
Publish-Subscribe 채널을 만들려면 <publish-subscribe-channel/> 요소(XML) 또는 PublishSubscribeChannel 인스턴스(Java)를 사용합니다.

Java

XML
<int:publish-subscribe-channel id="exampleChannel"/>
@Bean
public MessageChannel exampleChannel() {
    return new PublishSubscribeChannel();
}
또한 <queue/> 하위 요소를 사용하여 Pollable 채널 타입을 생성할 수도 있습니다. (상세 내용은 Message Channel Implementations 참고)

DirectChannel 구성

DirectChannel은 기본 채널 타입입니다.

Java

XML
<int:channel id="directChannel"/>
@Bean
public MessageChannel directChannel() {
    return new DirectChannel();
}
기본 채널은 라운드로빈(load-balancing) 방식과 failover가 활성화되어 있습니다.

이를 비활성화하려면 <dispatcher/> 하위 요소 또는 DirectChannel의 생성자/속성을 사용해 설정할 수 있습니다.
Java

XML
<int:channel id="failFastChannel">
    <int:dispatcher failover="false"/>
</channel>

<int:channel id="channelWithFixedOrderSequenceFailover">
    <int:dispatcher load-balancer="none"/>
</int:channel>
@Bean
public MessageChannel failFastChannel() {
    DirectChannel channel = new DirectChannel();
    channel.setFailover(false);
    return channel;
}

@Bean
public MessageChannel failFastChannel() {
    return new DirectChannel(null);
}
버전 6.3부터, UnicastingDispatcher 기반의 모든 MessageChannel 구현체는 단순 failover 옵션 대신 Predicate<Exception> 타입의 failoverStrategy를 사용해 예외 발생 시 다음 핸들러로 failover할지 여부를 결정할 수 있습니다.

더 복잡한 오류 분석이 필요하면 ErrorMessageExceptionTypeRouter를 활용하면 됩니다.


데이터 타입 채널 구성(Datatype Channel Configuration)

때로는, 소비자가 특정 페이로드 타입만 처리할 수 있어 입력 메시지의 페이로드 타입을 보장해야 할 때가 있습니다.

가장 먼저 떠오르는 방법은 **메시지 필터(Message Filter)**를 사용하는 것입니다.
그러나 메시지 필터는 단순히 요구사항에 맞지 않는 메시지를 걸러내는 것만 가능합니다.

또 다른 방법은 **컨텐츠 기반 라우터(Content-Based Router)**를 사용하여 요구사항에 맞지 않는 데이터 타입의 메시지를 특정 변환기(transformer)로 보내어 필요한 데이터 타입으로 변환하는 것입니다.
이 방법도 가능하지만, 더 간단하게 구현할 수 있는 방법이 있습니다: Datatype Channel 패턴을 적용하는 것입니다.

즉, 각 페이로드 데이터 타입마다 별도의 데이터 타입 채널을 사용하면 됩니다.

데이터 타입 채널 생성 예제

특정 페이로드 타입만 허용하는 데이터 타입 채널을 만들려면, 채널 요소의 datatype 속성에 **페이로드 클래스의 전체 경로(fully-qualified class name)**를 지정합니다.

<int:channel id="stringChannel" datatype="java.lang.String"/>
<int:channel id="integerChannel" datatype="java.lang.Integer"/>


위 예제에서는 stringChannel은 String 페이로드만, integerChannel은 Integer 페이로드만 받도록 구성됩니다.

@Bean
public MessageChannel numberChannel() {
    DirectChannel channel = new DirectChannel();
    channel.setDatatypes(Number.class);
    return channel;
}
<int:channel id="numberChannel" datatype="java.lang.Number"/>
타입 검사는 채널의 데이터 타입에 할당 가능한(assignable) 모든 타입에 대해 통과한다는 점에 유의하세요.
즉, 앞의 예제에서 numberChannel은 페이로드가 java.lang.Integer이거나 java.lang.Double인 메시지를 허용합니다.

여러 타입을 쉼표로 구분하여 지정할 수도 있으며, 다음 예제와 같습니다.
@Bean
public MessageChannel numberChannel() {
    DirectChannel channel = new DirectChannel();
    channel.setDatatypes(String.class, Number.class);
    return channel;
}
<int:channel id="stringOrNumberChannel" datatype="java.lang.String,java.lang.Number"/>
앞의 예제에서 numberChannel은 java.lang.Number 타입의 페이로드를 가진 메시지만 허용합니다.
그렇다면 메시지의 페이로드가 요구된 타입이 아닌 경우에는 어떻게 될까요?

이는 integrationConversionService라는 이름의 Spring Conversion Service 인스턴스 빈을 정의했는지 여부에 따라 달라집니다.
정의하지 않았다면, 즉시 예외가 발생합니다.
하지만 integrationConversionService 빈을 정의했다면, 메시지의 페이로드를 허용 가능한 타입으로 변환하려 시도하는 데 사용됩니다.

사용자 정의 컨버터를 등록할 수도 있습니다. 예를 들어, 앞서 설정한 numberChannel로 String 페이로드를 가진 메시지를 보낸 경우 다음과 같이 처리할 수 있습니다.
MessageChannel inChannel = context.getBean("numberChannel", MessageChannel.class);
inChannel.send(new GenericMessage<String>("5"));
일반적으로 이는 완전히 합법적인 작업이 될 수 있습니다.
하지만 Datatype Channel을 사용하기 때문에, 이러한 작업의 결과는 다음과 유사한 예외를 발생시킵니다.
Exception in thread "main" org.springframework.integration.MessageDeliveryException:
Channel 'numberChannel'
expected one of the following datataypes [class java.lang.Number],
but received [class java.lang.String]
…
예외가 발생하는 이유는 payload의 타입을 Number로 요구했지만, String을 전송했기 때문입니다.
따라서 String을 Number로 변환할 수 있는 무언가가 필요하며, 이를 위해 다음과 유사한 컨버터를 구현할 수 있습니다.
public static class StringToIntegerConverter implements Converter<String, Integer> {
    public Integer convert(String source) {
        return Integer.parseInt(source);
    }
}
그런 다음, Integration Conversion Service에 컨버터로 등록할 수 있습니다. 다음 예제가 보여줍니다.
@Bean
@IntegrationConverter
public StringToIntegerConverter strToInt {
    return new StringToIntegerConverter();
}
<int:converter ref="strToInt"/>

<bean id="strToInt" class="org.springframework.integration.util.Demo.StringToIntegerConverter"/>
또는 StringToIntegerConverter 클래스에 @Component 애노테이션을 붙여 자동 스캔하도록 할 수 있습니다.

converter 요소가 파싱될 때, integrationConversionService 빈이 아직 정의되어 있지 않다면 생성됩니다. 해당 컨버터가 설정되면, 이제 send 작업이 성공적으로 수행됩니다. 이는 datatype 채널이 String 페이로드를 Integer로 변환하기 위해 해당 컨버터를 사용하기 때문입니다.

페이로드 타입 변환에 대한 자세한 내용은 Payload Type Conversion을 참조하십시오.

버전 4.0부터, integrationConversionService는 DefaultDatatypeChannelMessageConverter에 의해 호출되며, 이는 애플리케이션 컨텍스트에서 변환 서비스를 조회합니다. 다른 변환 방식을 사용하려면, 채널의 message-converter 속성에 MessageConverter 구현체를 참조로 지정할 수 있습니다. 이 경우 fromMessage 메서드만 사용됩니다. 이 메서드는 컨버터가 메시지 헤더(예: content-type 정보)를 활용할 수 있도록 메시지에 접근할 수 있게 합니다. 메서드는 변환된 페이로드 또는 전체 Message 객체만 반환할 수 있습니다. 후자의 경우, 컨버터는 수신 메시지의 모든 헤더를 복사해야 합니다.

또는 MessageConverter 타입의 <bean/>을 datatypeChannelMessageConverter ID로 선언할 수 있으며, 이 컨버터는 datatype을 가진 모든 채널에서 사용됩니다.

QueueChannel 구성
QueueChannel을 생성하려면 <queue/> 하위 요소를 사용합니다. 채널의 용량은 다음과 같이 지정할 수 있습니다.
@Bean
public PollableChannel queueChannel() {
    return new QueueChannel(25);
}
<int:channel id="queueChannel">
    <queue capacity="25"/>
</int:channel>
'capacity' 속성에 값을 제공하지 않으면, 결과 큐는 무제한입니다. 메모리 부족과 같은 문제를 피하기 위해, 제한된 큐에 대해 명시적인 값을 설정하는 것이 강력히 권장됩니다.

Persistent QueueChannel 구성
QueueChannel은 메시지를 버퍼링할 수 있는 기능을 제공하지만 기본적으로 메모리 내에서만 동작하므로 시스템 장애 시 메시지가 손실될 가능성이 있습니다. 이러한 위험을 완화하기 위해, QueueChannel은 MessageGroupStore 전략 인터페이스의 영구 구현체를 백업으로 사용할 수 있습니다. MessageGroupStore와 MessageStore에 대한 자세한 내용은 Message Store를 참조하세요.

message-store 속성을 사용할 때는 capacity 속성을 사용할 수 없습니다.
QueueChannel이 메시지를 수신하면, 해당 메시지를 메시지 저장소에 추가합니다. QueueChannel에서 메시지를 폴링하면 메시지 저장소에서 제거됩니다.

기본적으로 QueueChannel은 메시지를 메모리 내 큐에 저장하므로 앞서 언급한 메시지 손실 시나리오가 발생할 수 있습니다. 그러나 Spring Integration은 JdbcChannelMessageStore와 같은 영구 저장소를 제공합니다.

QueueChannel에 메시지 저장소를 구성하려면 message-store 속성을 추가하면 됩니다.
<int:channel id="dbBackedChannel">
    <int:queue message-store="channelStore"/>
</int:channel>

<bean id="channelStore" class="o.s.i.jdbc.store.JdbcChannelMessageStore">
    <property name="dataSource" ref="dataSource"/>
    <property name="channelMessageStoreQueryProvider" ref="queryProvider"/>
</bean>
(아래 샘플에서는 Java/Kotlin 구성 옵션을 참조하세요.)

Spring Integration JDBC 모듈은 여러 인기 있는 데이터베이스에 대한 스키마 DDL도 제공합니다. 이 스키마들은 해당 모듈(spring-integration-jdbc)의 org.springframework.integration.jdbc.store.channel 패키지에 위치합니다.

중요한 특징 중 하나는 트랜잭션 영구 저장소(예: JdbcChannelMessageStore)를 사용할 때, poller에 트랜잭션이 구성되어 있는 한, 저장소에서 제거된 메시지는 트랜잭션이 성공적으로 완료될 경우에만 영구적으로 제거된다는 점입니다. 그렇지 않으면 트랜잭션이 롤백되고 메시지는 손실되지 않습니다.

Spring 관련 프로젝트가 점점 더 다양한 NoSQL 데이터 저장소를 지원하게 되면서, 메시지 저장소의 다른 구현체들도 많아지고 있습니다. 또한, 특정 요구사항을 만족하는 구현체를 찾을 수 없다면 MessageGroupStore 인터페이스를 직접 구현할 수도 있습니다.

버전 4.0 이후부터는 가능하다면 QueueChannel 인스턴스를 ChannelMessageStore를 사용하도록 구성하는 것이 권장됩니다. 이러한 저장소는 일반적인 메시지 저장소보다 해당 용도에 최적화되어 있습니다. 만약 ChannelMessageStore가 ChannelPriorityMessageStore인 경우, 메시지는 우선순위 순서 내에서 FIFO로 수신됩니다. 우선순위 개념은 메시지 저장소 구현체에 의해 결정됩니다. 예를 들어, 다음 예제는 MongoDB 채널 메시지 저장소를 위한 Java 구성을 보여줍니다.
@Bean
public BasicMessageGroupStore mongoDbChannelMessageStore(MongoDbFactory mongoDbFactory) {
    MongoDbChannelMessageStore store = new MongoDbChannelMessageStore(mongoDbFactory);
    store.setPriorityEnabled(true);
    return store;
}

@Bean
public PollableChannel priorityQueue(BasicMessageGroupStore mongoDbChannelMessageStore) {
    return new PriorityChannel(new MessageGroupQueue(mongoDbChannelMessageStore, "priorityQueue"));
}
//
@Bean
public IntegrationFlow priorityFlow(PriorityCapableChannelMessageStore mongoDbChannelMessageStore) {
    return IntegrationFlow.from((Channels c) ->
            c.priority("priorityChannel", mongoDbChannelMessageStore, "priorityGroup"))
            ....
            .get();
}
//
@Bean
fun priorityFlow(mongoDbChannelMessageStore: PriorityCapableChannelMessageStore) =
    integrationFlow {
        channel { priority("priorityChannel", mongoDbChannelMessageStore, "priorityGroup") }
    }

MessageGroupQueue 클래스에 주목하세요. 이는 MessageGroupStore 작업에 사용되는 BlockingQueue 구현체입니다.

QueueChannel 환경을 사용자 정의할 수 있는 또 다른 방법은 <int:queue> 하위 요소의 ref 속성이나 해당 생성자를 사용하는 것입니다. 이 속성은 임의의 java.util.Queue 구현체에 대한 참조를 제공합니다. 예를 들어, Hazelcast 분산 IQueue를 다음과 같이 구성할 수 있습니다.
@Bean
public HazelcastInstance hazelcastInstance() {
    return Hazelcast.newHazelcastInstance(new Config()
                                           .setProperty("hazelcast.logging.type", "log4j"));
}

@Bean
public PollableChannel distributedQueue() {
    return new QueueChannel(hazelcastInstance()
                              .getQueue("springIntegrationQueue"));
}

PublishSubscribeChannel 구성
PublishSubscribeChannel을 생성하려면 <publish-subscribe-channel/> 요소를 사용합니다. 이 요소를 사용할 때, 메시지 발행에 사용되는 task-executor를 지정할 수도 있습니다. (지정하지 않으면 발행이 송신자의 스레드에서 수행됩니다.) 예시는 다음과 같습니다.
@Bean
public MessageChannel pubsubChannel() {
    return new PublishSubscribeChannel(someExecutor());
}
<int:publish-subscribe-channel id="pubsubChannel" task-executor="someExecutor"/>
PublishSubscribeChannel 아래에 Resequencer 또는 Aggregator가 있는 경우, 채널의 apply-sequence 속성을 true로 설정할 수 있습니다. 이렇게 하면 채널이 메시지를 전달하기 전에 sequence-size와 sequence-number 메시지 헤더뿐만 아니라 correlation ID도 설정하도록 지정하는 것입니다. 예를 들어 구독자가 다섯 명인 경우, sequence-size는 5로 설정되고, 메시지의 sequence-number 헤더 값은 1에서 5까지 지정됩니다.

Executor와 함께 ErrorHandler도 구성할 수 있습니다. 기본적으로 PublishSubscribeChannel은 MessagePublishingErrorHandler 구현을 사용하여 에러를 errorChannel 헤더에 지정된 MessageChannel로 보내거나 글로벌 errorChannel 인스턴스로 보냅니다. Executor가 구성되지 않은 경우, ErrorHandler는 무시되며 예외가 직접 호출자 스레드로 전달됩니다.

아래 예제는 apply-sequence 헤더를 true로 설정하는 방법을 보여줍니다.
@Bean
public MessageChannel pubsubChannel() {
    PublishSubscribeChannel channel = new PublishSubscribeChannel();
    channel.setApplySequence(true);
    return channel;
}
<int:publish-subscribe-channel id="pubsubChannel" apply-sequence="true"/>
apply-sequence 값은 기본적으로 false로 설정되어 있어, Publish-Subscribe 채널이 동일한 메시지 인스턴스를 여러 아웃바운드 채널로 전송할 수 있습니다. Spring Integration은 payload와 header 참조의 불변성을 강제하므로, 이 플래그를 true로 설정하면 채널은 동일한 payload 참조를 가지지만 서로 다른 헤더 값을 가진 새로운 Message 인스턴스를 생성합니다.

버전 5.4.3부터 PublishSubscribeChannel은 BroadcastingDispatcher의 requireSubscribers 옵션을 사용하여, 구독자가 없을 때 메시지를 무시하지 않도록 구성할 수 있습니다. 이 옵션이 true로 설정된 경우 구독자가 없으면 MessageDispatchingException이 "Dispatcher has no subscribers" 메시지와 함께 발생합니다.

ExecutorChannel
ExecutorChannel을 생성하려면 <dispatcher> 서브엘리먼트에 task-executor 속성을 추가합니다. 이 속성의 값은 컨텍스트 내의 어떤 TaskExecutor도 참조할 수 있습니다. 예를 들어, 이렇게 하면 구독된 핸들러로 메시지를 디스패치하기 위한 스레드 풀을 구성할 수 있습니다. 앞서 언급했듯이, 이렇게 하면 송신자와 수신자 간의 단일 스레드 실행 컨텍스트가 깨지므로, 활성 트랜잭션 컨텍스트가 핸들러 호출에 의해 공유되지 않습니다(즉, 핸들러가 예외를 던져도 send 호출은 이미 성공적으로 반환된 상태입니다).

아래 예제는 <dispatcher> 엘리먼트를 사용하고 task-executor 속성에 실행기를 지정하는 방법을 보여줍니다.
@Bean
public MessageChannel executorChannel() {
    return new ExecutorChannel(someExecutor());
}
<int:channel id="executorChannel">
    <int:dispatcher task-executor="someExecutor"/>
</int:channel>

<dispatcher/> 서브엘리먼트에서도 앞서 DirectChannel 구성에서 설명한 것처럼 load-balancer와 failover 옵션을 모두 사용할 수 있습니다. 동일한 기본값이 적용되므로, 명시적으로 이 속성 중 하나 또는 둘을 설정하지 않으면 채널은 failover가 활성화된 라운드로빈 로드밸런싱 전략을 사용합니다.

예를 들어 다음과 같이 구성할 수 있습니다:

<int:channel id="executorChannelWithoutFailover">
    <int:dispatcher task-executor="someExecutor" failover="false"/>
</int:channel>

PriorityChannel Configuration
PriorityChannel을 생성하려면 <priority-queue/> 서브엘리먼트를 사용합니다. 다음 예제를 참조하십시오.
@Bean
public PollableChannel priorityChannel() {
    return new PriorityChannel(20);
}
<int:channel id="priorityChannel">
    <int:priority-queue capacity="20"/>
</int:channel>
기본적으로 이 채널은 메시지의 priority 헤더를 참조합니다. 그러나 대신 사용자 정의 Comparator 참조를 제공할 수도 있습니다. 또한 PriorityChannel은 다른 채널 타입과 마찬가지로 datatype 속성을 지원하며, QueueChannel과 마찬가지로 capacity 속성도 지원합니다. 다음 예제는 이러한 모든 기능을 보여줍니다.
@Bean
public PollableChannel priorityChannel() {
    PriorityChannel channel = new PriorityChannel(20, widgetComparator());
    channel.setDatatypes(example.Widget.class);
    return channel;
}
<int:channel id="priorityChannel" datatype="example.Widget">
    <int:priority-queue comparator="widgetComparator"
                    capacity="10"/>
</int:channel>
버전 4.0 이후로, priority-channel 하위 요소는 message-store 옵션을 지원합니다(이 경우 comparator와 capacity는 허용되지 않습니다). 메시지 스토어는 PriorityCapableChannelMessageStore여야 하며, 현재 Redis, JDBC, MongoDB용 PriorityCapableChannelMessageStore 구현체가 제공됩니다. 자세한 내용은 QueueChannel 구성 및 Message Store를 참조하십시오. 샘플 구성은 Backing Message Channels에서 확인할 수 있습니다.

RendezvousChannel 구성
RendezvousChannel은 queue 하위 요소가 <rendezvous-queue>일 때 생성됩니다. 이전에 설명한 것 외에 추가적인 구성 옵션은 제공되지 않으며, 이 채널의 큐는 용량 값(capacity)을 허용하지 않습니다. 이는 제로 용량 직접 전달(zero-capacity direct handoff) 큐이기 때문입니다. 다음 예제는 RendezvousChannel을 선언하는 방법을 보여줍니다.
@Bean
public PollableChannel rendezvousChannel() {
    return new RendezvousChannel();
}
<int:channel id="rendezvousChannel"/>
    <int:rendezvous-queue/>
</int:channel>

스코프 채널 구성
모든 채널은 다음 예제와 같이 scope 속성을 사용하여 구성할 수 있습니다.
<int:channel id="threadLocalChannel" scope="thread"/>

채널 인터셉터 구성
메시지 채널은 Channel Interceptors에서 설명한 것처럼 인터셉터를 가질 수 있습니다. <interceptors/> 하위 요소는 <channel/>(또는 더 구체적인 요소 타입)에 추가할 수 있습니다. ref 속성을 사용하여 ChannelInterceptor 인터페이스를 구현한 Spring 관리 객체를 참조할 수 있으며, 다음 예제가 이를 보여줍니다.
<int:channel id="exampleChannel">
    <int:interceptors>
        <ref bean="trafficMonitoringInterceptor"/>
    </int:interceptors>
</int:channel>
일반적으로 인터셉터 구현은 별도의 위치에 정의하는 것이 좋습니다. 인터셉터는 보통 여러 채널에서 재사용할 수 있는 공통 동작을 제공하기 때문입니다.

글로벌 채널 인터셉터 구성
채널 인터셉터는 개별 채널마다 횡단 관심사를 깔끔하고 간결하게 적용할 수 있는 방법을 제공합니다. 동일한 동작을 여러 채널에 적용해야 하는 경우, 각 채널에 동일한 인터셉터 세트를 반복해서 구성하는 것은 효율적이지 않습니다. 반복 구성을 피하면서 인터셉터가 여러 채널에 적용될 수 있도록, Spring Integration은 글로벌 인터셉터를 제공합니다. 다음 두 가지 예제를 참고하십시오.
<int:channel-interceptor pattern="input*, thing2*, thing1, !cat*" order="3">
    <bean class="thing1.thing2SampleInterceptor"/>
</int:channel-interceptor>

<int:channel-interceptor ref="myInterceptor" pattern="input*, thing2*, thing1, !cat*" order="3"/>

<bean id="myInterceptor" class="thing1.thing2SampleInterceptor"/>
각 <channel-interceptor/> 요소는 글로벌 인터셉터를 정의할 수 있으며, pattern 속성에 정의된 패턴과 일치하는 모든 채널에 적용됩니다. 앞의 예제에서는 글로벌 인터셉터가 'thing1' 채널과 'thing2' 또는 'input'으로 시작하는 모든 채널에 적용되지만, 'thing3'으로 시작하는 채널에는 적용되지 않습니다(버전 5.0부터).

패턴에 이러한 구문을 추가하면 하나의 가능한(비록 가능성은 낮지만) 문제가 발생할 수 있습니다. 예를 들어, 이름이 !thing1인 빈이 있고, 채널 인터셉터의 pattern 속성에 !thing1 패턴을 포함했다면, 이제 더 이상 일치하지 않습니다. 이 패턴은 이제 이름이 thing1이 아닌 모든 빈과 일치하게 됩니다. 이 경우 패턴에서 !를 \로 이스케이프하면 됩니다. 즉, \!thing1 패턴은 이름이 !thing1인 빈과 일치합니다.

order 속성은 특정 채널에 여러 인터셉터가 있을 때 이 인터셉터가 주입되는 순서를 관리하는 데 사용됩니다. 예를 들어, 'inputChannel' 채널은 로컬에서 개별 인터셉터가 구성될 수 있습니다(아래 예제 참조).
<int:channel id="inputChannel">
  <int:interceptors>
    <int:wire-tap channel="logger"/>
  </int:interceptors>
</int:channel>
합리적인 질문은 “글로벌 인터셉터가 로컬에서 구성된 다른 인터셉터나 다른 글로벌 인터셉터 정의와 어떤 관계로 주입되는가?”입니다. 현재 구현은 인터셉터 실행 순서를 정의하는 간단한 메커니즘을 제공합니다. order 속성에 양수를 지정하면 기존 인터셉터 뒤에 글로벌 인터셉터가 주입되며, 음수를 지정하면 기존 인터셉터 앞에 주입됩니다. 이는 앞의 예제에서 글로벌 인터셉터가 로컬에서 구성된 'wire-tap' 인터셉터 뒤에 주입되는 것을 의미합니다(왜냐하면 order 값이 0보다 크기 때문). 만약 패턴이 일치하는 다른 글로벌 인터셉터가 있다면, 두 인터셉터의 order 값을 비교하여 주입 순서가 결정됩니다. 기존 인터셉터보다 앞에 글로벌 인터셉터를 주입하려면 order 속성에 음수 값을 사용하면 됩니다.

order와 pattern 속성은 모두 선택 사항입니다. 기본값은 order는 0이고, pattern은 '*'로 모든 채널과 일치합니다.

Wire Tap

앞서 언급했듯, Spring Integration은 간단한 와이어 탭 인터셉터를 제공합니다. <interceptors/> 요소 내에서 어떤 채널에도 와이어 탭을 구성할 수 있습니다. 이는 디버깅에 특히 유용하며, Spring Integration의 로깅 채널 어댑터와 함께 사용할 수 있습니다.
<int:channel id="in">
    <int:interceptors>
        <int:wire-tap channel="logger"/>
    </int:interceptors>
</int:channel>

<int:logging-channel-adapter id="logger" level="DEBUG"/>
logging-channel-adapter는 또한 expression 속성을 지원하여 payload와 headers 변수를 대상으로 SpEL(Expression Language) 표현식을 평가할 수 있습니다. 또는 메시지 전체의 toString() 결과를 로깅하려면 log-full-message 속성에 true 값을 지정하면 됩니다. 기본값은 false로, 이 경우에는 payload만 로깅됩니다. true로 설정하면 payload뿐만 아니라 모든 헤더도 로깅됩니다. expression 옵션은 가장 유연하게 사용 가능하며, 예를 들어 expression="payload.user.name"와 같이 쓸 수 있습니다.

와이어 탭(wire tap)과 유사한 구성요소(Message Publishing Configuration)에 대한 일반적인 오해 중 하나는 이들이 자동으로 비동기적이라는 것입니다. 기본적으로 와이어 탭 자체는 비동기적으로 호출되지 않습니다. 대신 Spring Integration은 비동기 동작 구성을 위한 단일 통합 접근 방식으로 메시지 채널을 사용합니다. 메시지 흐름의 일부가 동기적이거나 비동기적인지는 해당 흐름에서 사용되는 메시지 채널의 유형에 따라 결정됩니다. 이것이 메시지 채널 추상화의 주요 장점 중 하나입니다. 프레임워크 설계 초기부터 메시지 채널을 주요 구성요소로 다루었으며, 단순히 EIP 패턴의 내부 구현이 아니라 최종 사용자가 구성 가능한 컴포넌트로 완전히 노출되어 있습니다.

와이어 탭 컴포넌트는 다음 작업만 수행합니다:

채널(예: channelA)을 통해 메시지 흐름을 가로챕니다.

각 메시지를 수집합니다.

메시지를 다른 채널(예: channelB)로 보냅니다.

본질적으로 브리지 패턴의 변형이지만, 채널 정의 안에 캡슐화되어 있어 흐름을 방해하지 않고 쉽게 활성화하거나 비활성화할 수 있습니다. 브리지와 달리 다른 메시지 흐름을 분기(fork)합니다. 이 흐름이 동기인지 비동기인지는 channelB의 메시지 채널 유형에 따라 달라집니다. 옵션으로는 DirectChannel, PollableChannel, ExecutorChannel이 있으며, 후자의 두 채널은 쓰레드 경계를 분리하여 메시지 디스패치가 메시지를 보낸 쓰레드와 다른 쓰레드에서 이루어지므로 비동기 통신이 됩니다. 이로써 와이어 탭 흐름이 동기적일지 비동기적일지가 결정됩니다.

이 접근 방식은 프레임워크 내 다른 컴포넌트(예: 메시지 퍼블리셔)와 일관되며, 특정 코드 조각을 동기 또는 비동기로 구현해야 하는지 미리 걱정할 필요 없이(단, 쓰레드 안전 코드는 작성해야 함) 단순함과 일관성을 제공합니다. 메시지 채널을 통해 두 코드 조각(A 컴포넌트와 B 컴포넌트)을 연결하는 실제 구성에 따라 동기/비동기가 결정됩니다. 나중에 동기에서 비동기로 전환하고 싶다면 메시지 채널만 변경하면 되므로 코드 수정 없이 전환이 가능합니다.

마지막으로 와이어 탭은 가능한 한 빨리 메시지를 전달하는 것이 일반적으로 바람직합니다. 따라서 와이어 탭의 아웃바운드 채널에 비동기 채널 옵션을 사용하는 경우가 많습니다. 그러나 비동기 동작은 기본적으로 강제되지 않습니다. 이를 강제하면 트랜잭션 경계가 깨질 수 있는 등 여러 사용 사례에서 문제가 발생할 수 있습니다. 예를 들어 감사(audit)를 위해 와이어 탭 패턴을 사용하는 경우, 감사 메시지는 원래 트랜잭션 내에서 보내야 할 수 있습니다. 예를 들어, 와이어 탭을 JMS 아웃바운드 채널 어댑터와 연결하면, 1) JMS 메시지를 트랜잭션 내에서 전송하면서도 2) “fire-and-forget” 동작을 수행하여 주 메시지 흐름에 지연이 생기지 않도록 할 수 있습니다.

버전 4.0부터는 인터셉터(예: WireTap)가 채널을 참조할 때 순환 참조(circular reference)를 피해야 합니다. 현재 인터셉터가 참조하는 채널을 제외하도록 패턴 또는 프로그래밍 방식으로 설정할 수 있습니다. 커스텀 ChannelInterceptor가 채널을 참조하는 경우 VetoCapableInterceptor를 구현하는 것이 좋습니다. 이렇게 하면 프레임워크가 제공된 패턴을 기반으로 각 후보 채널을 인터셉트해도 되는지 인터셉터에 문의합니다. 또한 인터셉터 메서드 내에서 런타임 보호를 추가하여 참조된 채널을 인터셉트하지 않도록 할 수 있습니다. WireTap은 이 두 가지 기법을 모두 사용합니다.

버전 4.3부터는 WireTap이 MessageChannel 인스턴스 대신 channelName을 받는 생성자를 추가로 제공합니다. 이는 Java 구성이나 채널 자동 생성 로직을 사용할 때 편리합니다. 대상 MessageChannel 빈은 인터셉터와 첫 상호작용 시점에 제공된 channelName에서 해결됩니다.

채널 해석에는 BeanFactory가 필요하므로, 와이어 탭 인스턴스는 반드시 Spring 관리 빈이어야 합니다. 이러한 지연 바인딩(late-binding) 접근 방식은 Java DSL 구성에서 일반적인 와이어 탭 패턴을 단순화할 수 있습니다.
@Bean
public PollableChannel myChannel() {
    return MessageChannels.queue()
            .wireTap("loggingFlow.input")
            .get();
}

@Bean
public IntegrationFlow loggingFlow() {
    return f -> f.log();
}
조건부 와이어 탭(Conditional Wire Taps)
와이어 탭은 selector 또는 selector-expression 속성을 사용하여 조건부로 만들 수 있습니다. selector는 MessageSelector 빈을 참조하며, 런타임 시 메시지를 탭 채널로 보낼지 여부를 결정할 수 있습니다. 마찬가지로 selector-expression은 불리언 SpEL 표현식으로 동일한 목적을 수행합니다. 표현식이 true로 평가되면 메시지가 탭 채널로 전송됩니다.

글로벌 와이어 탭(Global Wire Tap) 구성
글로벌 와이어 탭은 글로벌 채널 인터셉터(Global Channel Interceptor) 구성의 특별한 경우로 설정할 수 있습니다. 이를 위해 최상위 wire-tap 요소를 구성하면 됩니다. 이렇게 구성하면 일반적인 와이어 탭 네임스페이스 지원 외에도 pattern 및 order 속성을 사용할 수 있으며, 채널 인터셉터에서 작동하는 방식과 동일하게 동작합니다. 다음 예제는 글로벌 와이어 탭을 구성하는 방법을 보여줍니다.
@Bean
@GlobalChannelInterceptor(patterns = "input*,thing2*,thing1", order = 3)
public WireTap wireTap(MessageChannel wiretapChannel) {
    return new WireTap(wiretapChannel);
}
<int:wire-tap pattern="input*, thing2*, thing1" order="3" channel="wiretapChannel"/>
글로벌 와이어 탭은 기존 채널 구성을 수정하지 않고도 외부에서 단일 채널 와이어 탭을 구성할 수 있는 편리한 방법을 제공합니다. 이를 위해 pattern 속성을 대상 채널 이름으로 설정하면 됩니다. 예를 들어, 이 방법을 사용하여 채널의 메시지를 확인하는 테스트 케이스를 구성할 수 있습니다.