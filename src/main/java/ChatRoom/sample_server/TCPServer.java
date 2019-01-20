package ChatRoom.sample_server;

import ChatRoom.sample_server.handle.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPServer implements ClientHandler.ClientHandlerCallback {
    private final int port;
    private ClientListener mListener; // 开启一个线程来监听客户端的连接
    private List<ClientHandler> clientHandlerList = new ArrayList<>(); // 保存所有已连接的客户端

    public TCPServer(int port) {
        this.port = port;
    }

    /**
     * 在开启监听的时候可能会抛出异常
     * 正常启动 返回true，否则 返回false
     */
    public boolean start() {
        try {
            ClientListener listener = new ClientListener(port);
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

        for (ClientHandler handler : clientHandlerList) {
            handler.exit();
        }

        clientHandlerList.clear();
    }

    /**
     * 依次向已连接的客户端发送消息
     *
     * @param str 要发送的消息
     */
    public void broadcast(String str) {
        for (ClientHandler handler : clientHandlerList) {
            handler.send(str);
        }
    }

    @Override
    public void onSelfClosed(ClientHandler handler) {
        clientHandlerList.remove(handler);
    }

    @Override
    public void onNewMessageArrived(ClientHandler clientHandler, String message) {
        // 打印到屏幕
        System.out.println("Receive-" + clientHandler.getClientInfo() + ":" + message);
    }

    private class ClientListener extends Thread {
        private boolean done = false;
        private ServerSocket serverSocket;

        public ClientListener(int port) throws IOException {
            serverSocket = new ServerSocket(port);
            System.out.println("服务器信息：ip: " + serverSocket.getInetAddress() + ", port: " + serverSocket.getLocalPort());
        }

        @Override
        public void run() {
            System.out.println("服务端准备就绪");
            do {
                Socket client;
                try {
                    // 接收客户端的连接，如果发生异常，则重新开始
                    client = serverSocket.accept();
                } catch (IOException e) {
                    continue;
                }
                ClientHandler clientHandler;
                try {
                    clientHandler = new ClientHandler(client, TCPServer.this);
                    // 把客户端的连接添加到list中
                    clientHandlerList.add(clientHandler);
                    // 开启一个线程来处理从客户端读取到的信息
                    clientHandler.readToPrint();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (!done);
            System.out.println("服务器已关闭");
        }

        /**
         * 关闭操作，将done置为true，释放服务端连接资源
         */
        void exit() {
            done = true;
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
