package demo02_UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;

/**
 * UDP 服务提供者
 */
public class UDPProvider {

    public static void main(String[] args) throws IOException {
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);
        provider.start();

        int read = System.in.read();
        provider.exit();
    }

    private static class Provider extends Thread {
        private final String sn;
        private boolean done;
        private DatagramSocket datagramSocket;

        public Provider(String sn) {
            this.sn = sn;
        }

        @Override
        public void run() {
            System.out.println("UDPProvider Started...");
            try {
                // 作为一个接收者，指定一个端口用于数据的接收
                datagramSocket = new DatagramSocket(9999);

                while (!done) {
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
                    System.out.println("UDPProvider receive from ip: " + ip + ", port: " + port + ", data: " + receiveData);
                    int responsePort = MessageCreator.parsePort(receiveData);
                    if (responsePort != -1) {
                        // 构建一份回送数据
                        String responseData = MessageCreator.buildWithSN(sn);
                        byte[] responseDataBytes = responseData.getBytes();
                        DatagramPacket responsePacket = new DatagramPacket(responseDataBytes,
                                responseDataBytes.length,
                                receivePacket.getAddress(),
                                responsePort);

                        datagramSocket.send(responsePacket);
                    }
                }
            } catch (Exception e) {
            } finally {
                close();
            }
            System.out.println("UDPProvider Finished");
        }

        void exit() {
            done = true;
            close();
        }

        private void close() {
            if (datagramSocket != null) {
                datagramSocket.close();
                datagramSocket = null;
            }
        }
    }

}
