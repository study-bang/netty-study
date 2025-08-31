package com.example.two.aa_udp;

/**
1. UDP 소켓 수 제어
UDP 소켓 수는 서버가 바인딩하는 NioDatagramChannel 인스턴스의 개수로 결정됩니다. 각 채널은 독립적으로 작동하며 자체 데이터그램 스트림을 처리할 수 있습니다.

샘플 코드:
이 코드는 9000번과 9001번 포트에서 수신하는 두 개의 별도 UDP 서버를 생성합니다.

설명: b.bind(port)를 호출할 때마다 새로운 NioDatagramChannel 인스턴스가 생성됩니다. 이 코드는 두 개의 독립적인 UDP 서버를 동시에 실행하여 다른 포트 간에 부하를 분산하거나 다른 유형의 트래픽을 처리하는 데 유용합니다.
 */
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import java.nio.charset.StandardCharsets;

public class UdpMultiSocketServer {

    public void start(int port) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
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
                             String receivedMsg = msg.content().toString(StandardCharsets.UTF_8);
                             System.out.println("UDP Server on port " + port + " received: " + receivedMsg);
                         }
                     });
                 }
             });

            ChannelFuture future = b.bind(port).sync();
            System.out.println("UDP Server started on port " + port);
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 서로 다른 스레드에서 두 개의 별도 UDP 소켓 인스턴스를 생성하고 시작
        new Thread(() -> {
            try {
                new UdpMultiSocketServer().start(9000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                new UdpMultiSocketServer().start(9001);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
