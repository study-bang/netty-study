package com.example.echo.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class JsonMessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 바이트 버퍼에 읽을 데이터가 충분한지 확인
        if (in.readableBytes() < 1) {
            return;
        }

        // 바이트를 문자열로 변환
        String json = in.toString(StandardCharsets.UTF_8);
        // JSON 문자열을 객체로 변환
        JsonMessage msg = JsonMessage.fromJson(json);

        out.add(msg);
        in.skipBytes(in.readableBytes()); // 이미 읽은 바이트 처리
    }
}
