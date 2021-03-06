package demo04.client;

import demo04.bean.ServerInfo;

import java.io.IOException;

public class Client {

    public static void main(String[] args) {
        ServerInfo serverInfo = ClientSearcher.searchServer(10000);

        System.out.println("ServerInfo: " + serverInfo);

        if (serverInfo != null) {
            try {
                TCPClient.linkWith(serverInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
