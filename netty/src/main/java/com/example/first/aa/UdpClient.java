package com.example.first.aa;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class UdpClient {

    public void start(int port) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioDatagramChannel.class)
             .option(ChannelOption.SO_BROADCAST, true)
             .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                 @Override
                 protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
                     String receivedMsg = msg.content().toString(StandardCharsets.UTF_8);
                     System.out.println("UDP [" + port + "]: Received data: " + receivedMsg);
                 }
             });

            Channel ch = b.bind(0).sync().channel(); // 임의의 로컬 포트에 바인딩
            System.out.println("UDP client started on port " + port);

            // UDP 서버로 데이터 전송 (예시)
            // UDP 서버 주소를 명확하게 지정해야 합니다.
            String udpServerHost = "localhost";
            ChannelFuture f = ch.writeAndFlush(
                new DatagramPacket(
                    Unpooled.copiedBuffer("Hello from UDP " + port, StandardCharsets.UTF_8),
                    new InetSocketAddress(udpServerHost, port))
            ).sync();
            f.addListener(future -> {
                if(future.isSuccess()) {
                    System.out.println("UDP data sent successfully to port " + port);
                } else {
                    future.cause().printStackTrace();
                }
            });

            // 채널이 닫힐 때까지 대기
            ch.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
