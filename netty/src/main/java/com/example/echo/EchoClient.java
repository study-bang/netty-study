package com.example.echo;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.example.echo.codec.JsonMessage;
import com.example.echo.codec.JsonMessageDecoder;
import com.example.echo.codec.JsonMessageEncoder;
import com.google.gson.Gson;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class EchoClient {
    private final String host;
    private final int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        // 1. EventLoopGroup 설정 (클라이언트는 하나만 필요)
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            // 2. Bootstrap 설정
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class) // 클라이언트 소켓 채널 클래스 지정
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) {
                     ch.pipeline().addLast(
                        //  new StringDecoder(),
                        //  new StringEncoder(),
                        // StringDecoder, StringEncoder 대신 커스텀 코덱 사용
                        new JsonMessageDecoder(),
                        new JsonMessageEncoder(),
                         new EchoClientHandler()
                     );
                 }
             });

            // 3. 서버에 연결
            ChannelFuture f = b.connect(host, port).sync();
            System.out.println("Connected to " + host + ":" + port);

            // 4. 사용자 입력 받아서 서버로 전송
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String line = reader.readLine();
                if (line == null || "quit".equalsIgnoreCase(line)) {
                    break;
                }
                // f.channel().writeAndFlush(line);
                f.channel().writeAndFlush(new Gson().fromJson(line, JsonMessage.class));
            }

            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new EchoClient("localhost", 8888).start();
    }
}
/**
코드 상세 설명
EventLoopGroup 설정: 클라이언트는 서버에 연결하는 역할만 수행하므로 BossGroup 없이 하나의 EventLoopGroup만 사용합니다.

Bootstrap 설정: 클라이언트를 시작하는 데 사용되는 헬퍼 클래스입니다. group()과 channel() 설정은 서버와 유사합니다.

서버에 연결: b.connect(host, port) 메서드로 서버에 연결을 시도합니다. 이 작업은 비동기적으로 이루어지며, sync() 메서드로 작업이 완료될 때까지 기다립니다.

사용자 입력 전송: 사용자로부터 입력을 받아 f.channel().writeAndFlush(line)으로 서버에 전송합니다. 이 역시 비동기 작업입니다.

EchoClientHandler: 서버로부터 응답이 왔을 때 호출되는 channelRead() 메서드에서 받은 메시지를 출력합니다.
 */