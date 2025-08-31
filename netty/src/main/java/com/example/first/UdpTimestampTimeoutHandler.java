package com.example.first;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

public class UdpTimestampTimeoutHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    // 세션의 마지막 데이터 수신 시간을 저장
    private final Map<String, Long> lastReceiveTimes = new ConcurrentHashMap<>();
    
    // 각 세션의 타임아웃 스케줄을 저장
    private final Map<String, ScheduledFuture<?>> sessionTimeouts = new ConcurrentHashMap<>();
    
    // 타임아웃 시간 (1분)
    private static final long TIMEOUT_MINUTES = 1;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
        ByteBuf content = msg.content();
        String receivedData = content.toString(CharsetUtil.UTF_8);

        String[] parts = receivedData.split(":", 3);
        if (parts.length < 2) return;

        String sessionId = parts[0];
        String messageType = parts[1];
        
        // 데이터가 수신될 때마다 타임스탬프 갱신
        lastReceiveTimes.put(sessionId, System.currentTimeMillis());

        switch (messageType) {
            case "START":
                handleStart(ctx, sessionId);
                break;
            case "DATA":
                handleData(sessionId, parts.length > 2 ? parts[2] : "");
                break;
            case "END":
                handleEnd(sessionId);
                break;
        }
    }
    
    private void handleStart(ChannelHandlerContext ctx, String sessionId) {
        System.out.println("Session START received: " + sessionId);
        
        // 해당 세션에 대한 타임아웃 스케줄이 없으면 새로 생성
        if (!sessionTimeouts.containsKey(sessionId)) {
            scheduleTimeoutCheck(ctx, sessionId);
        }
    }

    private void handleData(String sessionId, String data) {
        // DATA 메시지 처리 로직
        System.out.println("Session DATA received: " + sessionId);
        // lastReceiveTimes 맵이 이미 갱신되었으므로 별도 로직 불필요
    }

    private void handleEnd(String sessionId) {
        lastReceiveTimes.remove(sessionId);
        ScheduledFuture<?> future = sessionTimeouts.remove(sessionId);
        if (future != null) {
            future.cancel(false);
            System.out.println("Session END received: " + sessionId + ". Timeout check cancelled.");
        }
    }

    private void scheduleTimeoutCheck(ChannelHandlerContext ctx, String sessionId) {
        // 500ms마다 타임아웃 여부를 체크하도록 예약 (잦은 데이터 수신 대비)
        ScheduledFuture<?> timeoutFuture = ctx.channel().eventLoop().schedule(() -> {
            Long lastTime = lastReceiveTimes.get(sessionId);
            if (lastTime == null) {
                // 세션이 이미 종료되었음
                return;
            }

            // 현재 시간과 마지막 수신 시간의 차이 계산
            long elapsed = System.currentTimeMillis() - lastTime;
            
            // 실제 타임아웃 시간이 지났는지 확인
            if (elapsed >= TimeUnit.MINUTES.toMillis(TIMEOUT_MINUTES)) {
                System.out.println("Session " + sessionId + " timed out! No message received for " + TIMEOUT_MINUTES + " minutes.");
                sessionTimeouts.remove(sessionId);
                lastReceiveTimes.remove(sessionId);
            } else {
                // 아직 타임아웃이 아님 -> 다시 체크하도록 재예약
                scheduleTimeoutCheck(ctx, sessionId);
            }
        }, 500, TimeUnit.MILLISECONDS);

        sessionTimeouts.put(sessionId, timeoutFuture);
    }

    // ----------------------------------- 매개변수 채널
    public void scheduleTask(Channel channel) {
        // Channel 객체를 통해 EventLoop에 접근
        ScheduledFuture<?> future = channel.eventLoop().schedule(() -> {
            System.out.println("Channel 객체를 통해 스케줄링된 작업 실행");
        }, 5, TimeUnit.SECONDS);
        
        System.out.println("5초 뒤에 실행될 작업이 스케줄링되었습니다.");
    }
}
