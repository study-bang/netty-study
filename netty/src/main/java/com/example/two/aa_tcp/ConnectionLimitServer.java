package com.example.two.aa_tcp;

/**
이 클래스는 Netty 서버를 설정하고 실행합니다. ConnectionLimitHandler를 파이프라인에 추가하여 연결 개수 제어 로직이 적용되도록 합니다.
 */
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ConnectionLimitServer {
    private final int port;

    public ConnectionLimitServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .option(ChannelOption.SO_BACKLOG, 100) // 백로그 큐 크기
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) {
                     ch.pipeline().addLast(
                         // 연결 개수 제어 로직을 가진 핸들러 추가
                         new ConnectionLimitHandler()
                     );
                 }
             });

            System.out.println("Server started, listening on port " + port);
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new ConnectionLimitServer(8888).run();
    }
}
