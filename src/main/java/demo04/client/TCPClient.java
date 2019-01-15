package demo04.client;

import demo04.bean.ServerInfo;
import demo04.clink.utils.CloseUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TCPClient {

    public static void linkWith(ServerInfo serverInfo) throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(3000);

        socket.connect(new InetSocketAddress(serverInfo.getAddress(), serverInfo.getPort()));
        System.out.println("【客户端】 IP: " + socket.getLocalAddress() + ", PORT: " + socket.getLocalPort());
        System.out.println("【服务端】 IP: " + socket.getInetAddress() + ", PORT: " + socket.getPort());

        try {
            ReadHandler readHandler = new ReadHandler(socket.getInputStream());
            readHandler.start();
            write(socket);
            readHandler.exit();
        } catch (Exception e) {
            System.out.println("连接异常断开");
        }
        socket.close();
        System.out.println("客户端已退出");
    }

    private static void write(Socket socket) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        PrintStream socketOutput = new PrintStream(socket.getOutputStream());

        do {
            String message = input.readLine();
            socketOutput.println(message);

            if ("bey!".equalsIgnoreCase(message))
                break;
        } while (true);

        socketOutput.close();
    }

    static class ReadHandler extends Thread {

        private boolean done = false;
        private final InputStream inputStream;

        public ReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {

            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
                do {
                    String receiveMsg;
                    try {
                        receiveMsg = input.readLine();
                    } catch (SocketTimeoutException e) {
                        continue;
                    }
                    if (receiveMsg == null) {
                        System.out.println("客户端已经无法读取数据");
                        break;
                    }
                    System.out.println(receiveMsg);
                } while (!done);
            } catch (Exception e) {
                if (!done) {
                    System.out.println("连接异常断开");
                }
            } finally {
                CloseUtils.close(inputStream);
            }
        }

        void exit() {
            done = true;
            CloseUtils.close(inputStream);
        }
    }
}
