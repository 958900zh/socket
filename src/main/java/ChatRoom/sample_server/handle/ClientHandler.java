package ChatRoom.sample_server.handle;

import ChatRoom.common.clink.utils.CloseUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {

    private final SocketChannel socketChannel; // 保存客户端的连接对象
    private final ClientReadHandler readHandler; //
    private final ClientWriteHandler writeHandler;
    private final ClientHandlerCallback clientHandlerCallback;
    private final String clientInfo;

    /**
     * 用于处理客户端的连接
     *
     * @param socketChannel                客户端的连接对象
     * @param clientHandlerCallback {@link ClientHandlerCallback} 回调方法，当客户端连接出现异常时被调用。
     */
    public ClientHandler(SocketChannel socketChannel, ClientHandlerCallback clientHandlerCallback) throws IOException {
        this.socketChannel = socketChannel;
        socketChannel.configureBlocking(false);

        Selector readSelector = Selector.open();
        socketChannel.register(readSelector, SelectionKey.OP_READ);
        this.readHandler = new ClientReadHandler(readSelector);

        Selector writeSelector = Selector.open();
        socketChannel.register(writeSelector, SelectionKey.OP_WRITE);
        this.writeHandler = new ClientWriteHandler(writeSelector);

        this.clientHandlerCallback = clientHandlerCallback;
        this.clientInfo = socketChannel.getRemoteAddress().toString();
        System.out.println("新客户端连接：" + clientInfo);
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public void exit() {
        readHandler.exit();
        writeHandler.exit();
        CloseUtils.close(socketChannel);
        System.out.println("客户端退出：ip: " + clientInfo);
    }

    public void send(String str) {
        writeHandler.send(str);
    }

    public void readToPrint() {
        readHandler.start();
    }

    private void exitBySelf() {
        exit();
        clientHandlerCallback.onSelfClosed(this);
    }

    public interface ClientHandlerCallback {
        // 自身关闭通知
        void onSelfClosed(ClientHandler handler);

        // 收到消息时通知
        void onNewMessageArrived(ClientHandler clientHandler, String message);
    }

    /**
     * 用于单独处理客户端的读取操作
     */
    class ClientReadHandler extends Thread {

        private boolean done = false;
        private final Selector selector;
        private final ByteBuffer byteBuffer;

        public ClientReadHandler(Selector selector) {
            this.selector = selector;
            this.byteBuffer = ByteBuffer.allocate(256);
        }

        @Override
        public void run() {
            try {
                do {
                    if (selector.select() == 0) {
                        if (done)
                            break;
                        continue;
                    }
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        if (done)
                            break;
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        if (key.isReadable()) {
                            SocketChannel client = (SocketChannel) key.channel();
                            byteBuffer.clear();
                            int read = client.read(byteBuffer);
                            if (read > 0) {
                                String receiveMsg = new String(byteBuffer.array(), 0, byteBuffer.position() - 1);
                                clientHandlerCallback.onNewMessageArrived(ClientHandler.this, receiveMsg);
                            } else {
                                System.out.println("客户端已经无法读取数据");
                                ClientHandler.this.exitBySelf();
                                break;
                            }

                        }
                    }
                } while (!done);
            } catch (Exception e) {
                if (!done) {
                    System.out.println("连接异常断开");
                    ClientHandler.this.exitBySelf();
                }
            } finally {
                CloseUtils.close(selector);
            }
        }

        void exit() {
            done = true;
            selector.wakeup();
            CloseUtils.close(selector);
        }
    }

    /**
     * 用于单独处理客户端的写入操作
     */
    class ClientWriteHandler {
        private boolean done = false;
        private final Selector selector;
        private final ByteBuffer byteBuffer;
        private final ExecutorService executorService;

        public ClientWriteHandler(Selector selector) {
            this.selector = selector;
            this.byteBuffer = ByteBuffer.allocate(256);
            this.executorService = Executors.newFixedThreadPool(1);
        }

        public void send(String str) {
            if (done)
                return;
            executorService.submit(new WriteRunnable(str));
        }

        void exit() {
            done = true;
            CloseUtils.close(selector);
            executorService.shutdownNow();
        }

        class WriteRunnable implements Runnable {
            private String msg;

            public WriteRunnable(String msg) {
                this.msg = msg + '\n';
            }

            @Override
            public void run() {
                if (done)
                    return;

                byteBuffer.clear();
                byteBuffer.put(msg.getBytes());
                byteBuffer.flip();
                while (!done && byteBuffer.hasRemaining()) {
                    try {
                        int len = socketChannel.write(byteBuffer);
                        if (len < 0) {
                            System.out.println("客户端已经无法发送数据");
                            ClientHandler.this.exitBySelf();
                            break;
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }
}
