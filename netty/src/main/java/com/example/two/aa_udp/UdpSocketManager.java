package com.example.two.aa_udp;

/**
UDP 통신은 비연결성이므로, "포트 별로 UDP 서버를 10개 초과해서 생성"하는 것 자체는 기술적으로 문제가 없습니다. 하지만 말씀하신 맥락은 아마도 동시에 활성화된 UDP 서버/소켓의 개수를 제한하려는 의도일 것입니다.

1. 10개 초과 시 동작
기본적으로 Netty는 bind() 호출 횟수에 제한을 두지 않습니다. 따라서 포트 별로 UDP 서버를 10개, 20개, 또는 100개까지 생성해도 Netty 자체는 오류를 발생시키지 않습니다.

그러나 시스템 레벨에서는 다음과 같은 문제가 발생할 수 있습니다.

포트 바인딩 충돌: 만약 동일한 포트에 여러 번 바인딩하려고 시도하면 BindException 오류가 발생합니다. 이는 한 번에 하나의 프로세스만 특정 포트에 바인딩할 수 있기 때문입니다.

리소스 한계: 운영체제가 허용하는 파일 디스크립터(파일, 소켓 등)의 최대 개수를 초과하면 더 이상 소켓을 생성할 수 없어 오류가 발생합니다.

성능 저하: 너무 많은 소켓을 생성하면 메모리와 CPU 리소스 소모가 커져 전체 시스템 성능이 저하될 수 있습니다.

2. 10개 초과 시 제어하는 방법
TCP와 달리 UDP는 연결 개념이 없어 ChannelGroup을 통한 직접적인 개수 제어가 어렵습니다. 대신, 애플리케이션 레벨에서 생성된 소켓의 개수를 직접 관리해야 합니다.

가장 좋은 방법은 소켓 인스턴스를 저장하는 컬렉션을 만들고, 이 컬렉션의 크기를 확인하여 제한을 두는 것입니다.

샘플 코드: UDP 소켓 생성 개수 제어
다음 코드는 UDP 서버를 생성할 때마다 소켓을 Map에 저장하고, 그 크기를 확인하여 10개를 초과하면 더 이상 생성하지 않도록 합니다.

activeUdpChannels: ConcurrentHashMap을 사용하여 포트 번호와 Channel 객체를 저장합니다.

createUdpServer():

메서드 시작 부분에서 activeUdpChannels.size()를 확인하여 MAX_UDP_SOCKETS를 초과하는지 검사합니다.

제한을 초과하면 false를 반환하고, 초과하지 않으면 소켓을 생성하여 맵에 추가합니다.

이 방법을 사용하면 UDP 소켓의 총 개수를 애플리케이션 레벨에서 정확하게 제어할 수 있습니다.
 */
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UdpSocketManager {

    // 활성화된 UDP 소켓들을 관리하는 맵
    private static final Map<Integer, Channel> activeUdpChannels = new ConcurrentHashMap<>();
    private static final int MAX_UDP_SOCKETS = 10;
    
    private final EventLoopGroup group = new NioEventLoopGroup();

    public boolean createUdpServer(int port) {
        if (activeUdpChannels.size() >= MAX_UDP_SOCKETS) {
            System.out.println("UDP 소켓 생성 한도 초과: " + activeUdpChannels.size() + "개. 추가 생성 불가.");
            return false;
        }

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioDatagramChannel.class)
             .handler(new ChannelInitializer<NioDatagramChannel>() {
                 @Override
                 protected void initChannel(NioDatagramChannel ch) {
                     ch.pipeline().addLast(new SimpleChannelInboundHandler<DatagramPacket>() {
                         @Override
                         protected void channelRead0(io.netty.channel.ChannelHandlerContext ctx, DatagramPacket msg) {
                             System.out.println("Port " + port + ": received data.");
                         }
                     });
                 }
             });

            Channel channel = b.bind(port).sync().channel();
            activeUdpChannels.put(port, channel);
            System.out.println("UDP 서버 생성: Port " + port + ". 현재 활성 소켓: " + activeUdpChannels.size());
            return true;

        } catch (Exception e) {
            System.err.println("포트 " + port + " 바인딩 실패: " + e.getMessage());
            return false;
        }
    }

    public void shutdownAll() {
        activeUdpChannels.values().forEach(Channel::close);
        group.shutdownGracefully();
    }
}

// 이 UdpSocketManager를 사용하는 예시
class Main {
    public static void main(String[] args) {
        UdpSocketManager manager = new UdpSocketManager();
        
        // 10개까지는 성공적으로 생성
        for (int i = 0; i < 12; i++) {
            int port = 9000 + i;
            manager.createUdpServer(port);
        }
        
        manager.shutdownAll();
    }
}
