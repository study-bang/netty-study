package com.example.two.aa_tcp;

/**
이 핸들러는 ChannelGroup을 사용하여 모든 활성 연결을 추적하고, 새로운 연결이 들어올 때마다 최대 허용치(MAX_CONNECTIONS)를 초과하는지 확인합니다.
 */
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ConnectionLimitHandler extends ChannelInboundHandlerAdapter {

    // 최대 허용 연결 수 (10개로 설정)
    private static final int MAX_CONNECTIONS = 10;

    // 활성화된 채널들을 스레드 안전하게 관리하는 그룹
    private static final ChannelGroup channels = 
        new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 새로운 연결이 활성화될 때 호출
        System.out.println("새로운 연결 요청 감지. 현재 활성 연결 수: " + channels.size());
        
        if (channels.size() >= MAX_CONNECTIONS) {
            System.out.println("--- 연결 한도 초과! 새로운 연결을 거부합니다. ---");
            ctx.close(); // 연결을 즉시 닫아버림
            return;
        }

        channels.add(ctx.channel()); // 채널 그룹에 추가
        System.out.println("새로운 연결 추가됨. 현재 연결 수: " + channels.size());
        ctx.fireChannelActive(); // 다음 핸들러로 이벤트 전파
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // 연결이 비활성화(종료)될 때 호출
        channels.remove(ctx.channel()); // 채널 그룹에서 제거
        System.out.println("연결 종료. 현재 연결 수: " + channels.size());
        ctx.fireChannelInactive(); // 다음 핸들러로 이벤트 전파
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
