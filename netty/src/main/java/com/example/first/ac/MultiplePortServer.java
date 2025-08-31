package com.example.first.ac;

// MultiplePortServer.java
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

public class MultiplePortServer {
    private final int tcpPort;

    public MultiplePortServer(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ch.pipeline().addLast(
                         new StringDecoder(), 
                         new PortDistributorHandler() // 여러 포트를 분배하는 핸들러
                     );
                 }
             });

            ChannelFuture f = b.bind(tcpPort).sync();
            System.out.println("TCP server for multiple ports started on " + tcpPort);
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new MultiplePortServer(8888).start();
    }
}
