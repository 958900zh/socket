package demo04.client;

import demo04.bean.ServerInfo;

import java.io.*;
import java.net.*;

public class TCPClient {

    public static void linkWith(ServerInfo serverInfo) throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(3000);

        socket.connect(new InetSocketAddress(serverInfo.getAddress(), serverInfo.getPort()));
        System.out.println("【客户端】 IP: " + socket.getLocalAddress() + ", PORT: " + socket.getLocalPort());
        System.out.println("【服务端】 IP: " + socket.getInetAddress() + ", PORT: " + socket.getPort());

        try {
            send(socket);
        } catch (Exception e) {
            System.out.println("连接异常断开");
        }
        socket.close();
        System.out.println("客户端已退出");
    }

    private static void send(Socket socket) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        PrintStream socketOutput = new PrintStream(socket.getOutputStream());

        BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        boolean flag = true;
        do {
            String message = input.readLine();
            socketOutput.println(message);

            String returnMsg = socketInput.readLine();
            if ("bey".equalsIgnoreCase(returnMsg))
                flag = false;
            else
                System.out.println("服务端返回：" + returnMsg);
        } while (flag);

        socketInput.close();
        socketOutput.close();
    }
}
