package demo04.server;

import demo04.server.handle.ClientHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPServer {
    private final int port;
    private ClientListener mListener;
    private List<ClientHandler> clientHandlerList = new ArrayList<>();

    public TCPServer(int port) {
        this.port = port;
    }

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

    public void stop() {
        if (mListener != null)
            mListener.exit();

        for (ClientHandler handler : clientHandlerList) {
            handler.exit();
        }

        clientHandlerList.clear();
    }

    public void broadcast(String str) {
        for (ClientHandler handler : clientHandlerList) {
            handler.send(str);
        }
    }

    private static class ClientListener extends Thread {
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
                    client = serverSocket.accept();
                } catch (IOException e) {
                    continue;
                }
                ClientHandler clientHandler = new ClientHandler(client);
                clientHandler.readToPrint();
            } while (!done);
            System.out.println("服务器已关闭");
        }

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
