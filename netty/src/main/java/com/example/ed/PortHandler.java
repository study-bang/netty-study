package com.example.ed;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class PortHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // TCP로 받은 메시지(문자열)에서 포트 번호를 추출
        String portData = (String) msg;
        System.out.println("Received port data via TCP: " + portData);

        // 예시: "8000,8001,8002"와 같은 형식의 문자열을 파싱
        List<Integer> udpPorts = Arrays.stream(portData.split(","))
                                       .map(Integer::parseInt)
                                       .collect(Collectors.toList());
        
        // 각 포트 번호에 대해 UDP 클라이언트 시작
        for (Integer port : udpPorts) {
            startUdpClient(port);
        }
    }

    private void startUdpClient(int port) {
        // UDP 클라이언트 시작 로직
        // 별도의 스레드에서 UDP 클라이언트 부트스트랩을 실행하는 것이 좋습니다.
        new Thread(() -> {
            try {
                // 이 부분은 다음 섹션에서 상세히 설명
                new UdpClient().start(port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
