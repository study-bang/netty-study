package com.example.first.ac;

// PortDistributorHandler.java
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PortDistributorHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // TCP로 받은 메시지에서 포트 번호를 파싱
        String portData = (String) msg;
        System.out.println("Received port data via TCP: " + portData);

        // 예시: "8000,8001,8002"를 파싱
        List<Integer> udpPorts = Arrays.stream(portData.split(","))
                                       .map(String::trim)
                                       .map(Integer::parseInt)
                                       .collect(Collectors.toList());

        InetSocketAddress clientAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientHost = clientAddress.getHostString();
        
        // 각 포트 번호에 대해 UDP 통신 시작
        for (Integer port : udpPorts) {
            System.out.println("Starting UDP communication for port: " + port);
            // 각 UDP 통신을 별도의 스레드에서 시작
            new Thread(() -> {
                try {
                    new UdpManager().startUdpCommunication(clientHost, port);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
