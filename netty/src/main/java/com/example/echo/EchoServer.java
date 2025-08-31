package com.example.echo;

import com.example.echo.codec.JsonMessageDecoder;
import com.example.echo.codec.JsonMessageEncoder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class EchoServer {
    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        // 1. EventLoopGroup 설정
        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // 연결 요청을 수락하는 스레드
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 연결된 소켓의 I/O를 처리하는 스레드

        try {
            // 2. ServerBootstrap 설정
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class) // 서버 소켓 채널 클래스 지정
             .option(ChannelOption.SO_BACKLOG, 128) // 연결 요청 큐 크기 설정
             .childOption(ChannelOption.SO_KEEPALIVE, true) // TCP KeepAlive 설정
             .childHandler(new ChannelInitializer<SocketChannel>() { // 새로운 연결 시 파이프라인 설정
                @Override
                public void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(
                        // new StringDecoder(), // 바이트를 문자열로 디코딩
                        // new StringEncoder(), // 문자열을 바이트로 인코딩
                        // StringDecoder, StringEncoder 대신 커스텀 코덱 사용
                        new JsonMessageDecoder(),
                        new JsonMessageEncoder(),
                        new EchoServerHandler() // 비즈니스 로직 핸들러
                    );
                }
            });

            System.out.println("Echo Server started on port " + port);

            // 3. 서버 바인딩 및 대기
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new EchoServer(8888).start();
    }
}
/**
코드 상세 설명
EventLoopGroup 설정: bossGroup은 들어오는 연결 요청을 수락하고, workerGroup은 수락된 연결의 I/O 작업을 처리하는 스레드 풀입니다. 이렇게 역할을 분리하여 효율적인 리소스 관리를 가능하게 합니다.

ServerBootstrap 설정: 서버를 시작하는 데 필요한 모든 설정을 담는 헬퍼 클래스입니다. group()으로 EventLoopGroup을 지정하고, channel()로 **NioServerSocketChannel**을 사용해 NIO 기반의 논블로킹 서버 소켓을 생성합니다. option()은 서버 소켓 자체의 옵션을, childOption()은 연결된 클라이언트 소켓의 옵션을 설정합니다.

ChannelInitializer: 새로운 SocketChannel이 생성될 때마다 호출되어, 해당 채널의 **ChannelPipeline**에 ChannelHandler들을 추가합니다.

StringDecoder: 클라이언트로부터 받은 바이트 데이터를 문자열로 변환하여 EchoServerHandler에 전달합니다.

StringEncoder: EchoServerHandler에서 보낸 문자열을 네트워크 전송을 위한 바이트 데이터로 변환합니다.

EchoServerHandler: 실제 비즈니스 로직이 담긴 커스텀 핸들러입니다.

EchoServerHandler: ChannelInboundHandlerAdapter를 상속받아 channelRead() 메서드를 오버라이드합니다.

channelRead()는 클라이언트로부터 데이터를 수신했을 때 호출됩니다. ctx.writeAndFlush(msg)를 통해 받은 메시지를 다시 클라이언트에게 보냅니다. write()는 쓰기 작업을 큐에 담고, flush()는 실제 네트워크 전송을 시작합니다.
 */