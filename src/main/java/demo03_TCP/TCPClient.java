package demo03_TCP;

import java.io.*;
import java.net.*;

public class TCPClient {

    private static final int REMOTE_PORT = 20000;
    private static final int LOCAL_PORT = 20002;

    public static void main(String[] args) throws IOException {
        Socket socket = createSocket();

        initSocket(socket);

        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), REMOTE_PORT));

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
        // 获取socket的输出流（写入到服务端）
        OutputStream outputStream = socket.getOutputStream();

        // 获取socket的输入流（接收服务端的数据）
        InputStream inputStream = socket.getInputStream();

        byte[] buffer = Tools.intToByteArray(3255698);
        outputStream.write(buffer);

        // 读取服务端返回的数据
        int read = inputStream.read(buffer);
        if (read > 0)
            System.out.println("收到数量: " + read + " 数据: " + Tools.byteArraysToInt(buffer));
        else
            System.out.println("没有收到: " + read);

        // 释放资源（千万不能忘）
        outputStream.close();
        inputStream.close();
    }

    private static Socket createSocket() throws IOException {

        Socket socket = new Socket();
        socket.bind(new InetSocketAddress(Inet4Address.getLocalHost(), LOCAL_PORT));

        return socket;
    }

    private static void initSocket(Socket socket) throws SocketException {
        socket.setSoTimeout(2000);

        socket.setReuseAddress(true);

        socket.setTcpNoDelay(true);

        socket.setKeepAlive(true);

        // 对于close关闭操作行为进行怎样的处理；默认为false，0
        // false，0: 默认情况，关闭时立即返回，底层系统接管输出流，将缓冲区内的数据发送完成
        // true，0: 关闭时立即返回，缓冲区数据抛弃，直接发送RST结束命令到对方，并无需经过2MSL等待
        // true，200: 关闭时最长阻塞200毫秒，随后按第二种情况处理
        socket.setSoLinger(true, 20);

        // 是否让紧急数据内敛，默认false；紧急数据通过 socket.sendUrgentData(1);发送
        socket.setOOBInline(true);

        socket.setReceiveBufferSize(64 * 1024 * 1024);
        socket.setSendBufferSize(64 * 1024 * 1024);

        // 设置性能参数：短连接、延迟、带宽的相对重要性
        socket.setPerformancePreferences(1, 1, 1);
    }
}
