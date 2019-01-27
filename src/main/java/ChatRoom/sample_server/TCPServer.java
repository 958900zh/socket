package ChatRoom.sample_server;

import ChatRoom.common.clink.utils.CloseUtils;
import ChatRoom.sample_server.handle.ClientHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer implements ClientHandler.ClientHandlerCallback {
    private final int port;
    private ClientListener mListener; // 开启一个线程来监听客户端的连接
    private List<ClientHandler> clientHandlerList = new ArrayList<>(); // 保存所有已连接的客户端
    private final ExecutorService forwardingThreadPoolExecutor;
    private Selector selector;
    private ServerSocketChannel server;

    public TCPServer(int port) {
        this.port = port;
        this.forwardingThreadPoolExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     * 在开启监听的时候可能会抛出异常
     * 正常启动 返回true，否则 返回false
     */
    public boolean start() {
        try {
            selector = Selector.open();
            ServerSocketChannel server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.socket().bind(new InetSocketAddress(port));
            System.out.println("服务器信息：ip: " + server.getLocalAddress());
            server.register(selector, SelectionKey.OP_CONNECT);
            this.server = server;
            ClientListener listener = new ClientListener();
            mListener = listener;
            listener.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 首先退出服务端，然后依次退出已连接的客户端，最后清空list
     */
    public void stop() {
        if (mListener != null)
            mListener.exit();

        CloseUtils.close(server);
        CloseUtils.close(selector);
        synchronized (TCPServer.this) {
            for (ClientHandler handler : clientHandlerList) {
                handler.exit();
            }
        }

        clientHandlerList.clear();
        forwardingThreadPoolExecutor.shutdownNow();
    }

    /**
     * 依次向已连接的客户端发送消息
     *
     * @param str 要发送的消息
     */
    public synchronized void broadcast(String str) {
        for (ClientHandler handler : clientHandlerList) {
            handler.send(str);
        }
    }

    @Override
    public synchronized void onSelfClosed(ClientHandler handler) {
        clientHandlerList.remove(handler);
    }

    @Override
    public void onNewMessageArrived(ClientHandler clientHandler, String message) {
        // 打印到屏幕
        System.out.println("Receive-" + clientHandler.getClientInfo() + ":" + message);
        forwardingThreadPoolExecutor.submit(() -> {
            for (ClientHandler handler : clientHandlerList) {
                if (!handler.equals(clientHandler))
                    handler.send(message);
            }
        });
    }

    private class ClientListener extends Thread {
        private boolean done = false;

        @Override
        public void run() {
            System.out.println("服务端准备就绪");

            Selector selector = TCPServer.this.selector;

            do {
                try {
                    // 接收客户端的连接，如果发生异常，则重新开始
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

                        if (key.isAcceptable()) {
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                            SocketChannel socketChannel = serverSocketChannel.accept();

                            ClientHandler clientHandler;
                            try {
                                clientHandler = new ClientHandler(socketChannel, TCPServer.this);
                                synchronized (TCPServer.this) {
                                    // 把客户端的连接添加到list中
                                    clientHandlerList.add(clientHandler);
                                }
                                // 开启一个线程来处理从客户端读取到的信息
                                clientHandler.readToPrint();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (IOException e) {
                    continue;
                }

            } while (!done);
            System.out.println("服务器已关闭");
        }

        /**
         * 关闭操作，将done置为true，释放服务端连接资源
         */
        void exit() {
            done = true;
            selector.wakeup();
        }
    }

}
