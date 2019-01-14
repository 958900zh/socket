package demo04.server.handle;

import demo04.clink.utils.CloseUtils;

import java.io.*;
import java.net.Socket;

public class ClientHandler {

    private final Socket socket;
    private final ClientReadHandler readHandler;
    private boolean flag = true;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.readHandler = new ClientReadHandler(socket.getInputStream());
        System.out.println("新客户端连接：ip: " + socket.getInetAddress() + ", port: " + socket.getPort());
    }

    public void exit() {
        readHandler.exit();
        CloseUtils.close(socket);
        System.out.println("客户端退出：ip: " + socket.getInetAddress() + ", port: " + socket.getPort());
    }

    public void send(String str) {

    }

    public void readToPrint() {
        readHandler.start();
    }

    private void exitBySelf() {
        exit();

    }

    class ClientReadHandler extends Thread {

        private boolean done = false;
        private final InputStream inputStream;

        public ClientReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {

            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
                do {
                    String receiveMsg = input.readLine();
                    if (receiveMsg == null) {
                        System.out.println("客户端已经无法读取数据");
                        ClientHandler.this.exitBySelf();
                        break;
                    }
                    System.out.println(receiveMsg);
                } while (!done);
            } catch (Exception e) {
                if (!done) {
                    System.out.println("连接异常断开");
                    ClientHandler.this.exitBySelf();
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
