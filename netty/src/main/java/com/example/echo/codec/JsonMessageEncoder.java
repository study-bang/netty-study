package com.example.echo.codec;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class JsonMessageEncoder extends MessageToByteEncoder<JsonMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, JsonMessage msg, ByteBuf out) {
        String json = msg.toJson();
        out.writeBytes(json.getBytes(StandardCharsets.UTF_8));
    }
}
