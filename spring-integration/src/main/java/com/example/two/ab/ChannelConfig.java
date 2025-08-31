package com.example.two.ab;

/**
wire-tap의 역할
<int:wire-tap>은 스프링 인티그레이션에서 메시지 흐름을 **도청(tapping)**하는 기능을 수행합니다. 쉽게 말해, 메인 메시지 흐름을 방해하지 않고 메시지 복사본을 다른 채널로 보내는 역할을 해요.

channel="logChannel": 이는 wire-tap이 도청한 메시지 복사본을 **logChannel**이라는 이름의 또 다른 채널로 보내라는 의미예요.

이 구성은 메시지가 loggingChannel에 도착하면, 메시지의 원본은 계속해서 다음 엔드포인트로 진행하고, 동시에 복사본은 logChannel로 전송되는 구조를 만듭니다.

    <int:channel id="loggingChannel">
        <int:interceptors>
            <int:wire-tap channel="logChannel" />
        </int:interceptors>
    </int:channel>
    을 자바로 구현하는 부분.

    아래 추가 설명
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.interceptor.WireTap;
import org.springframework.messaging.MessageChannel;

@Configuration
public class ChannelConfig {

    @Bean
    public MessageChannel loggingChannel() {
        DirectChannel channel = new DirectChannel();
        // Create a WireTap that sends messages to the 'logChannel' bean
        // and add it as an interceptor to the 'loggingChannel'.
        channel.addInterceptor(new WireTap(logChannel()));
        return channel;
    }

    @Bean
    public MessageChannel logChannel() {
        // This is the channel that receives copies of messages from loggingChannel
        return new PublishSubscribeChannel();
    }
}
/**
채널 인터셉터를 사용하지 않고 메인 흐름에 영향을 주지 않으면서 메시지를 비동기적으로 처리하는 방법은 **PublishSubscribeChannel**과 wire-tap 엔드포인트를 활용하는 것입니다. 이 방식은 메인 흐름의 엔드포인트를 하나 더 추가하는 것처럼 동작합니다.

PublishSubscribeChannel을 이용한 비동기 처리
PublishSubscribeChannel은 메시지 하나를 여러 소비자에게 동시에, 비동기적으로 전달하는 채널입니다. 이 채널에 메시지를 보내면, 이 채널을 구독하는 모든 핸들러들이 병렬로 메시지를 받아서 처리할 수 있습니다.

핵심 원리는 다음과 같습니다.

메인 메시지 흐름이 메시지를 PublishSubscribeChannel로 보냅니다.

PublishSubscribeChannel은 메시지 복사본을 만들어 모든 구독자에게 보냅니다.

구독자들은 각각의 스레드에서 메시지를 독립적으로 처리합니다.

xml 방식
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:int="http://www.springframework.org/schema/integration"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/integration
        https://www.springframework.org/schema/integration/spring-integration.xsd">

    <int:publish-subscribe-channel id="broadcastChannel" />

    <int:channel id="inputChannel" />

    <int:bridge input-channel="inputChannel" output-channel="broadcastChannel" />

    <int:service-activator 
        input-channel="broadcastChannel" 
        ref="subscriberOne" 
        method="process" />

    <int:service-activator 
        input-channel="broadcastChannel" 
        ref="subscriberTwo" 
        method="process" />
        
</beans>
```
<int:publish-subscribe-channel id="broadcastChannel" />: 이 태그는 PublishSubscribeChannel 타입의 채널을 생성합니다. 이 채널에 도착하는 메시지는 모든 구독자에게 전달됩니다.

<int:bridge>: 메시지를 한 채널에서 다른 채널로 전달하는 간단한 컴포넌트입니다. 여기서는 inputChannel로 들어온 메시지를 broadcastChannel로 전달하는 역할을 해요.

<int:service-activator input-channel="broadcastChannel" ... />: broadcastChannel을 input-channel로 지정한 두 개의 service-activator가 있습니다. 이들은 broadcastChannel의 **구독자(subscriber)**가 됩니다.
 */