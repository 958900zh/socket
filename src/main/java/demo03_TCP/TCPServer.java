package demo03_TCP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

public class TCPServer {

    private static final int PORT = 20000;

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = createServerSocket();

        initServerSocket(serverSocket);

        serverSocket.bind(new InetSocketAddress(Inet4Address.getLocalHost(), PORT), 50);

        System.out.println("服务端已启动");

        for (; ; ) {
            // accept()是阻塞方法，直到有socket客户端接入，才会向下执行
            Socket client = serverSocket.accept();
            // 对于每一个客户端的连接，开启一个新的线程去处理连接
            ClientHandler clientHandler = new ClientHandler(client);
            clientHandler.start();
        }
    }

    private static void initServerSocket(ServerSocket serverSocket) throws SocketException {
        serverSocket.setReuseAddress(true);

        serverSocket.setReceiveBufferSize(64 * 1024 * 1024);

        // 设置 accept 的超时时间
//        serverSocket.setSoTimeout(2000);

        serverSocket.setPerformancePreferences(1, 1, 1);
    }

    private static ServerSocket createServerSocket() throws IOException {
        ServerSocket serverSocket = new ServerSocket();

        return serverSocket;
    }

    /**
     * 处理连接到server的client，开启一个新的线程来处理
     */
    private static class ClientHandler extends Thread {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("新的客户端已经连接 IP: " + socket.getInetAddress() + ", PORT: " + socket.getPort());
            try {
                // 获取socket的输入流（客户端发出的数据）
                InputStream inputStream = socket.getInputStream();

                // 获取socket的输出流（写回到客户端的数据）
                OutputStream outputStream = socket.getOutputStream();

                byte[] buffer = new byte[128];
                int read = inputStream.read(buffer);
                if (read > 0) {
                    System.out.println("收到数量: " + read + " 数据: " + Tools.byteArraysToInt(buffer));
                    outputStream.write(buffer, 0, read);
                } else {
                    System.out.println("没有收到: " + read);
                    outputStream.write(Tools.intToByteArray(0));
                }

                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                System.out.println("连接异常断开");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("客户端退出 IP: " + socket.getInetAddress() + ", PORT: " + socket.getPort());
        }
    }
}
