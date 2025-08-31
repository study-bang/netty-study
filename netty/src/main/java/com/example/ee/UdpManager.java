package com.example.ee;

// UdpManager.java
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.buffer.Unpooled;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class UdpManager {

    public void startUdpCommunication(String remoteHost, int udpPort) {
        // UDP 서버 및 클라이언트 역할 모두를 수행하는 부트스트랩
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioDatagramChannel.class) // UDP 통신을 위한 채널
             .option(ChannelOption.SO_BROADCAST, true)
             .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                 @Override
                 protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
                     // UDP 데이터 수신
                     String receivedMsg = msg.content().toString(StandardCharsets.UTF_8);
                     System.out.println("UDP Server received: " + receivedMsg + " from " + msg.sender());
                 }
             });

            // UDP 서버 역할: 특정 포트(udpPort)에 바인딩
            Channel serverChannel = b.bind(udpPort).sync().channel();
            System.out.println("UDP Server listening on port " + udpPort);

            // UDP 클라이언트 역할: 원격 주소로 데이터 전송
            Channel clientChannel = b.bind(0).sync().channel(); // 임의의 포트에 바인딩
            InetSocketAddress remoteAddress = new InetSocketAddress(remoteHost, udpPort);

            // 데이터 전송
            String message = "Hello, I received your address via TCP! (from UDP)";
            clientChannel.writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer(message, StandardCharsets.UTF_8),
                remoteAddress
            )).sync();

            // 채널이 닫힐 때까지 대기
            serverChannel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            group.shutdownGracefully();
        }
    }
}
