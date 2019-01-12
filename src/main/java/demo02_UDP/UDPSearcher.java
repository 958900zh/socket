package demo02_UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * UDP 服务搜索方
 */
public class UDPSearcher {

    private static final int LISTEN_PORT = 30000;

    public static void main(String[] args) throws IOException {
        System.out.println("UDPSearcher Started...");
        Listener listen = listen();
        sendBroadcast();

        System.in.read();
        List<Device> deviceList = listen.getDeviceListAndClose();
        for (Device device : deviceList) {
            System.out.println(device);
        }
    }

    private static class Device {
        private String ip;
        private int port;
        private String sn;

        public Device(String ip, int port, String sn) {
            this.ip = ip;
            this.port = port;
            this.sn = sn;
        }

        @Override
        public String toString() {
            return "Device{" +
                    "ip='" + ip + '\'' +
                    ", port=" + port +
                    ", sn='" + sn + '\'' +
                    '}';
        }
    }

    private static class Listener extends Thread {
        private final int listenPort;
        private final List<Device> deviceList = new ArrayList<>();
        private boolean done = false;
        private DatagramSocket datagramSocket = null;

        public Listener(int listenPort) {
            this.listenPort = listenPort;
        }

        @Override
        public void run() {
            try {
                datagramSocket = new DatagramSocket(listenPort);

                while (!done) {
                    byte[] buf = new byte[512];
                    DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                    datagramSocket.receive(datagramPacket);

                    String ip = datagramPacket.getAddress().getHostAddress();
                    int port = datagramPacket.getPort();
                    int length = datagramPacket.getLength();
                    String receiveData = new String(datagramPacket.getData(), 0, length);
                    System.out.println("UDPSearcher receive from ip: " + ip + ", port: " + port + ", data: " + receiveData);
                    String parseSN = MessageCreator.parseSN(receiveData);
                    if (parseSN != null) {
                        Device device = new Device(ip, port, parseSN);
                        deviceList.add(device);
                    }
                }
            } catch (Exception e) {
            } finally {
                close();
            }
        }

        private void close() {
            if (datagramSocket != null) {
                datagramSocket.close();
                datagramSocket = null;
            }
        }

        List<Device> getDeviceListAndClose() {
            done = true;
            close();
            return deviceList;
        }
    }

    private static Listener listen() {
        Listener listener = new Listener(LISTEN_PORT);
        listener.start();
        return listener;
    }

    private static void sendBroadcast() throws IOException {
        System.out.println("UDPSearcher sendBroadcast start...");
        // 作为一个搜索方，通过系统自动分配端口
        DatagramSocket datagramSocket = new DatagramSocket();

        // 构建一份发送数据
        String requestData = MessageCreator.buildWithPort(LISTEN_PORT);
        byte[] requestDataBytes = requestData.getBytes();
        DatagramPacket requestPacket = new DatagramPacket(requestDataBytes,
                requestDataBytes.length);
        // 本机8888端口
        requestPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        requestPacket.setPort(9999);

        // 发送数据
        datagramSocket.send(requestPacket);
        datagramSocket.close();
        System.out.println("UDPSearcher sendBroadcast finish...");
    }

    private void test() throws IOException {
        System.out.println("UDPSearcher listen Started...");
        DatagramSocket datagramSocket = new DatagramSocket(LISTEN_PORT);

        // 构建一个接收实体
        byte[] buf = new byte[512];
        DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

        // 接收数据
        datagramSocket.receive(receivePacket);

        // 打印接收到的相关信息
        String ip = receivePacket.getAddress().getHostAddress();
        int port = receivePacket.getPort();
        int length = receivePacket.getLength();
        String receiveData = new String(receivePacket.getData(), 0, length);
        System.out.println("UDPSearcher receive from ip: " + ip + ", port: " + port + ", data: " + receiveData);
        String parseSN = MessageCreator.parseSN(receiveData);
        if (parseSN != null) {

        }
        System.out.println("UDPSearcher Finished");
        datagramSocket.close();
    }
}
