package com.example.two.aa_udp;

/**
2. 별도 스레드 풀을 이용한 제어
UDP에서 실제 병목 현상은 소켓의 수가 아니라 들어오는 데이터를 처리하는 속도입니다. 처리 로직이 느리면 EventLoop 스레드를 차단하여 다른 패킷을 읽지 못하게 됩니다. 이 문제를 해결하기 위해 무거운 작업을 별도의 스레드 풀로 넘기는 것이 좋습니다.

샘플 코드:
이 코드는 EventExecutorGroup을 사용하여 동시 데이터 처리 작업 수를 10개로 제한합니다.

설명: EventLoopGroup은 모든 네트워크 I/O를 효율적으로 처리하지만, 무거운 작업은 즉시 별도의 workerPool로 넘깁니다. 이로써 네트워크 I/O와 비즈니스 로직이 분리되어 EventLoop는 자유롭게 더 많은 패킷을 읽을 수 있습니다. workerPool의 크기(THREAD_POOL_SIZE)는 동시에 처리할 수 있는 데이터 패킷의 수를 직접적으로 제어합니다.
 */
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import java.nio.charset.StandardCharsets;

public class UdpThreadpoolServer {
    private static final int THREAD_POOL_SIZE = 10;
    private static final int PORT = 9000;

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        // 무거운 처리를 위한 별도의 스레드 풀
        EventExecutorGroup workerPool = new DefaultEventExecutorGroup(THREAD_POOL_SIZE);

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioDatagramChannel.class)
             .option(ChannelOption.SO_BROADCAST, true)
             // I/O 스레드(group)가 데이터 수신을 처리
             .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                 @Override
                 protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
                     final String data = msg.content().toString(StandardCharsets.UTF_8);
                     
                     // 무거운 처리를 workerPool로 오프로드
                     workerPool.submit(() -> {
                         System.out.println("Processing data in a separate thread: " + data);
                         // 무거운 작업 시뮬레이션
                         try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
                     });
                 }
             });
            
            b.bind(PORT).sync().channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
            workerPool.shutdownGracefully();
        }
    }
}
