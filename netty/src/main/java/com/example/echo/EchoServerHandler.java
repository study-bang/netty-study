package com.example.echo;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

// 4. 비즈니스 로직을 담은 핸들러
@ChannelHandler.Sharable // 이 핸들러는 여러 채널에서 공유 가능
public class EchoServerHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("Received message: " + msg);
        // 받은 메시지를 그대로 클라이언트에게 되돌려 보냄
        ChannelFuture f = ctx.writeAndFlush(msg); // 받은 메시지를 그대로 다시 보냄
        // 작업이 완료되면 실행될 리스너 등록
        f.addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("데이터 전송 성공!");
            } else {
                System.out.println("데이터 전송 실패!");
                future.cause().printStackTrace();
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 예외 발생 시 로그 출력 및 채널 닫기
        cause.printStackTrace();
        ctx.close
        ();
    }
}
/**
channelRead: 클라이언트로부터 데이터를 수신했을 때 호출됩니다. ctx.writeAndFlush(msg)는 받은 메시지를 클라이언트에게 다시 보냅니다.

exceptionCaught: 예외가 발생했을 때 호출됩니다.

ChannelFuture의 역할:

작업 상태 확인: 비동기 작업(예: 데이터 전송, 연결)이 성공했는지, 실패했는지, 또는 완료되었는지 확인할 수 있습니다.

리스너 등록: 작업이 완료되었을 때 실행될 콜백 함수(GenericFutureListener)를 등록할 수 있습니다.

 */
