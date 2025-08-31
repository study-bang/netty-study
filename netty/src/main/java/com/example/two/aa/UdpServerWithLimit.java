package com.example.two.aa;

/**
2. UDP 연결 개수 제어
UDP는 비연결성(connectionless) 프로토콜이라서 TCP처럼 "연결 개수"라는 개념이 없습니다. 대신 동시에 처리할 수 있는 데이터그램 소켓의 개수를 제어하는 방식으로 유사한 효과를 얻을 수 있습니다.

UDP 소켓 수: TCP와 달리 UDP는 bind()를 통해 생성된 NioDatagramChannel의 개수만큼 소켓을 가질 수 있습니다. 여러 개의 UDP 소켓을 만들어 각각 다른 포트에서 데이터를 수신하거나, 동일한 포트에 SO_REUSEADDR 옵션을 사용해 바인딩할 수도 있습니다.

스레드 풀 제어: UDP 데이터 처리 핸들러에서 시간이 오래 걸리는 작업이 있다면, 이를 별도의 EventExecutorGroup 스레드 풀로 넘겨서 동시에 처리할 수 있는 작업의 개수를 제한할 수 있습니다.
 */
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public class UdpServerWithLimit {

    public void start() {
        EventLoopGroup group = new NioEventLoopGroup();
        // 동시 실행 가능한 작업 수를 10개로 제한
        EventExecutorGroup workerPool = new DefaultEventExecutorGroup(10);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioDatagramChannel.class)
             .option(ChannelOption.SO_BROADCAST, true)
             .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                 @Override
                 protected void channelRead0(io.netty.channel.ChannelHandlerContext ctx, DatagramPacket msg) {
                     // I/O 스레드를 블로킹하지 않도록, 오래 걸리는 작업은 별도 스레드 풀로 넘김
                     workerPool.submit(() -> {
                         // 복잡한 데이터 처리, DB 접근 등
                         System.out.println("Processing data in a separate thread.");
                     });
                 }
             });
            b.bind(9000).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
            workerPool.shutdownGracefully();
        }
    }
}
