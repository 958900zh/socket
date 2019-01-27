package ChatRoom.common.clink.core;

import java.io.Closeable;
import java.nio.channels.SocketChannel;

public interface IoProvider extends Closeable {

    boolean registerInput(SocketChannel socketChannel, HandlerInputCallback callback);

    boolean registerOutput(SocketChannel socketChannel, HandlerOutputCallback callback);

    void unRegisterInput(SocketChannel socketChannel);

    void unRegisterOutput(SocketChannel socketChannel);

    abstract class HandlerInputCallback implements Runnable {

        @Override
        public void run() {
            canProviderInput();
        }

        protected abstract void canProviderInput();
    }

    abstract class HandlerOutputCallback implements Runnable {
        private Object attach;

        @Override
        public void run() {
            canProviderOutput(attach);
        }

        public void setAttach(Object attach) {
            this.attach = attach;
        }

        protected abstract void canProviderOutput(Object attach);
    }
}
