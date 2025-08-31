package echo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

import com.example.echo.codec.JsonMessage;
import com.example.echo.codec.JsonMessageDecoder;

// EmbeddedChannel을 이용한 디코더 테스트 예제
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;

public class JsonMessageDecoderTest {
    @Test
    public void testDecoder() {
        EmbeddedChannel channel = new EmbeddedChannel(new JsonMessageDecoder());
        String json = "{\"type\":\"test\",\"data\":\"hello\"}";
        channel.writeInbound(Unpooled.wrappedBuffer(json.getBytes()));

        JsonMessage decodedMessage = channel.readInbound();
        assertEquals("test", decodedMessage.getType());
        assertEquals("hello", decodedMessage.getData());
    }
}
