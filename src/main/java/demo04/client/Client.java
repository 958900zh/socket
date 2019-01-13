package demo04.client;

import demo04.bean.ServerInfo;

public class Client {

    public static void main(String[] args) {
        ServerInfo serverInfo = ClientSearcher.searchServer(10000);

        System.out.println("ServerInfo: " + serverInfo);
    }
}
