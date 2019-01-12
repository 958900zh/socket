package demo03_TCP;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TCPClient {

    private static final int REMOTE_PORT = 20000;
    private static final int LOCAL_PORT = 20001;
    public static void main(String[] args) throws IOException {
        // 创建socket对象
        Socket socket = new Socket();
        // 连接服务端，由于服务端在本地，IP:127.0.0.1，port:8888
        socket.connect(new InetSocketAddress("127.0.0.1", 8888));

        // 打印客户端与服务端的信息
        System.out.println("【客户端】 IP: " + socket.getLocalAddress() + ", PORT: " + socket.getLocalPort());
        System.out.println("【服务端】 IP: " + socket.getInetAddress() + ", PORT: " + socket.getPort());

        // 发送数据
        try {
            send(socket);
        } catch (IOException e) {
            System.out.println("异常关闭");
        }

        // 释放资源（不能忘）
        socket.close();
        System.out.println("客户端退出");
    }

    /**
     * 向服务端发送数据
     *
     * @param socket
     */
    private static void send(Socket socket) throws IOException {
        // 获取键盘的输入流，并转换为BufferedReader
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        // 获取socket的输出流（写入到服务端），并转换为PrintStream
        OutputStream outputStream = socket.getOutputStream();
        PrintStream socketOutput = new PrintStream(outputStream);

        // 获取socket的输入流（接收服务端的数据），并转换为BufferedReader
        InputStream inputStream = socket.getInputStream();
        BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));

        boolean flag = true;
        do {
            // 读取键盘输入的数据，并写入到socket的输出流中
            String message = input.readLine();
            socketOutput.println(message); // 一定要调用 println方法才会向服务端写入

            // 读取服务端返回的数据
            String returnMsg = socketInput.readLine();
            if ("bey".equalsIgnoreCase(returnMsg))
                flag = false;
            else
                System.out.println("服务端返回的数据是：" + returnMsg);
        } while (flag);

        // 释放资源（千万不能忘）
        socketInput.close();
        socketOutput.close();
    }
}
