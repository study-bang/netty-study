package com.example.first.ac;

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

    public void startUdpCommunication(String remoteHost, int udpPort) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioDatagramChannel.class)
             .option(ChannelOption.SO_BROADCAST, true)
             .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                 @Override
                 protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
                     // UDP 데이터 수신
                     String receivedMsg = msg.content().toString(StandardCharsets.UTF_8);
                     System.out.println("UDP Server on port [" + udpPort + "] received: " + receivedMsg + " from " + msg.sender());
                 }
             });

            // UDP 서버 역할: 특정 포트(udpPort)에 바인딩
            Channel serverChannel = b.bind(udpPort).sync().channel();
            System.out.println("UDP Server listening on port " + udpPort + " started");

            // UDP 클라이언트 역할: 원격 주소로 데이터 전송
            Channel clientChannel = b.bind(0).sync().channel(); // 임의의 포트에 바인딩
            InetSocketAddress remoteAddress = new InetSocketAddress(remoteHost, udpPort);

            String message = "Hello from UDP client for port " + udpPort;
            clientChannel.writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer(message, StandardCharsets.UTF_8),
                remoteAddress
            )).sync();

            serverChannel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
