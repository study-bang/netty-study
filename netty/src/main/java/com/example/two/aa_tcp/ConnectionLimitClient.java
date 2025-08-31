package com.example.two.aa_tcp;

/**
이 클라이언트는 서버에 15개의 연결을 동시에 시도하여 서버의 연결 제한이 올바르게 작동하는지 테스트합니다. MAX_CONNECTIONS가 10이므로, 11번째부터는 연결이 거부되는 것을 확인할 수 있습니다.
 */
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ConnectionLimitClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8888;
    private static final int CLIENT_COUNT = 15;

    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            IntStream.range(0, CLIENT_COUNT).forEach(i -> {
                Bootstrap b = new Bootstrap();
                b.group(group)
                 .channel(NioSocketChannel.class)
                 .handler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     protected void initChannel(SocketChannel ch) {
                         // 클라이언트 핸들러는 필요에 따라 추가
                         ch.pipeline().addLast(new SimpleClientHandler(i));
                     }
                 });
                b.connect(HOST, PORT).addListener(future -> {
                    if (future.isSuccess()) {
                        System.out.println("Client " + i + ": Connection successful!");
                    } else {
                        System.out.println("Client " + i + ": Connection failed.");
                    }
                });
            });

            // 클라이언트들이 작업을 완료할 시간을 주기 위해 잠시 대기
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}

class SimpleClientHandler extends ChannelInboundHandlerAdapter {
    private final int clientId;

    public SimpleClientHandler(int clientId) {
        this.clientId = clientId;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client " + clientId + " is active.");
    }
    
    // 추가 핸들러 로직 (메시지 송수신 등)은 여기에 구현
}
