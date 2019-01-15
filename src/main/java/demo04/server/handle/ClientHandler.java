package demo04.server.handle;

import demo04.clink.utils.CloseUtils;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {

    private final Socket socket; // 保存客户端的连接对象
    private final ClientReadHandler readHandler; //
    private final ClientWriteHandler writeHandler;
    private final CloseNotify closeNotify;

    /**
     * 用于处理客户端的连接
     * @param socket 客户端的连接对象
     * @param closeNotify {@link CloseNotify} 回调方法，当客户端连接出现异常时被调用。
     */
    public ClientHandler(Socket socket, CloseNotify closeNotify) throws IOException {
        this.socket = socket;
        this.readHandler = new ClientReadHandler(socket.getInputStream());
        this.writeHandler = new ClientWriteHandler(socket.getOutputStream());
        this.closeNotify = closeNotify;
        System.out.println("新客户端连接：ip: " + socket.getInetAddress() + ", port: " + socket.getPort());
    }

    public void exit() {
        readHandler.exit();
        writeHandler.exit();
        CloseUtils.close(socket);
        System.out.println("客户端退出：ip: " + socket.getInetAddress() + ", port: " + socket.getPort());
    }

    public void send(String str) {
        writeHandler.send(str);
    }

    public void readToPrint() {
        readHandler.start();
    }

    private void exitBySelf() {
        exit();
        closeNotify.onSelfClosed(this);
    }

    public interface CloseNotify {
        void onSelfClosed(ClientHandler handler);
    }

    /**
     * 用于单独处理客户端的读取操作
     */
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

    /**
     * 用于单独处理客户端的写入操作
     */
    class ClientWriteHandler {
        private boolean done = false;
        private final PrintStream printStream;
        private final ExecutorService executorService;

        public ClientWriteHandler(OutputStream outputStream) {
            this.printStream = new PrintStream(outputStream);
            this.executorService = Executors.newFixedThreadPool(1);
        }

        public void send(String str) {
            executorService.submit(new WriteRunnable(str));
        }

        void exit() {
            done = true;
            CloseUtils.close(printStream);
            executorService.shutdownNow();
        }

        class WriteRunnable implements Runnable {
            private String msg;

            public WriteRunnable(String msg) {
                this.msg = msg;
            }

            @Override
            public void run() {
                if (done)
                    return;
                try {
                    printStream.println(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
