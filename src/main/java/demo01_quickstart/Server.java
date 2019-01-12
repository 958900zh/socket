package demo01_quickstart;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws IOException {
        // 创建ServerSocket对象，并设置开启的端口号，客户端通过此端口号进行数据传输
        ServerSocket serverSocket = new ServerSocket(8888);
        System.out.println("服务端已启动");

        for (; ; ) {
            // accept()是阻塞方法，直到有socket客户端接入，才会向下执行
            Socket client = serverSocket.accept();
            // 对于每一个客户端的连接，开启一个新的线程去处理连接
            ClientHandler clientHandler = new ClientHandler(client);
            clientHandler.start();
        }
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
                // 获取socket的输入流（客户端发出的数据），并转换为BufferedReader
                InputStream inputStream = socket.getInputStream();
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));

                // 获取socket的输出流（写回到客户端的数据），并转换为PrintStream
                OutputStream outputStream = socket.getOutputStream();
                PrintStream socketOutput = new PrintStream(outputStream);

                boolean flag = true;
                do {
                    String receiveMsg = socketInput.readLine();
                    if ("bey".equalsIgnoreCase(receiveMsg)) {
                        flag = false;
                        socketOutput.println("bey");
                    } else {
                        System.out.println("客户端发来的数据是：" + receiveMsg);
                        socketOutput.println(receiveMsg.length());
                    }
                } while (flag);

                socketInput.close();
                socketOutput.close();
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
