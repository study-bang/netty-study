package com.example.ee;

// AddressHandler.java
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.net.InetSocketAddress;

public class AddressHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // TCP 클라이언트의 원격 주소 추출
        InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        System.out.println("TCP Client connected from: " + remoteAddress.getHostString());

        // UDP 서버 및 클라이언트 시작 (IP 주소와 포트를 전달)
        // 여기서는 예시로 UDP 포트 9000을 사용합니다.
        new UdpManager().startUdpCommunication(remoteAddress.getHostString(), 9000);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
