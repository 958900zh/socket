package ChatRoom.common.clink.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class IoArgs {
    private byte[] bytes = new byte[256];
    private ByteBuffer buffer = ByteBuffer.wrap(bytes);

    public int read(SocketChannel socketChannel) throws IOException {
        buffer.clear();
        return socketChannel.read(buffer);
    }

    public int write(SocketChannel socketChannel) throws IOException {
        return socketChannel.write(buffer);
    }

    public String bufferString() {
        return new String(buffer.array(), 0, buffer.position() - 1);
    }

    public interface IoArgsEventListener {
        void onStarted(IoArgs args);
        void onCompleted(IoArgs args);
    }
}
