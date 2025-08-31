package com.example.two.aa;

/**
1. TCP 연결 개수 제어
TCP는 연결 지향 프로토콜이므로, 서버 측에서 연결 개수를 직접 제어할 수 있는 방법들이 있습니다.

백로그(Backlog) 큐 크기: ServerBootstrap의 option(ChannelOption.SO_BACKLOG, int) 옵션은 동시에 대기할 수 있는 연결 요청의 최대 개수를 설정합니다. 이 값을 초과하는 연결 요청은 거부됩니다.

리스너 수: ServerBootstrap은 bossGroup에 설정된 EventLoop 수만큼 새로운 연결을 수락하는 리스너 스레드를 가집니다. NioEventLoopGroup(1)과 같이 EventLoop 수를 제한하여 연결 수락 속도를 제어할 수 있습니다.

자체 연결 관리: ChannelGroup을 사용하여 활성화된 모든 Channel 객체를 직접 관리할 수 있습니다. ChannelGroup의 크기가 특정 임계값에 도달하면 더 이상 새로운 연결을 받지 않거나, 특정 연결을 강제로 끊는 로직을 구현할 수 있습니다.
 */
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ConnectionLimitHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    // 최대 허용 연결 수
    private static final int MAX_CONNECTIONS = 100;

    // 활성화된 채널들을 관리하는 그룹
    private static final ChannelGroup channels = 
        new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (channels.size() >= MAX_CONNECTIONS) {
            System.out.println("Connection limit reached. Rejecting new connection.");
            ctx.close();
            return;
        }
        channels.add(ctx.channel());
        System.out.println("New connection added. Total connections: " + channels.size());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
        // 메시지 처리 로직
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        channels.remove(ctx.channel());
        System.out.println("Connection closed. Total connections: " + channels.size());
    }
}
