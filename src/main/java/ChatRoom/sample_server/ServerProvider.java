package ChatRoom.sample_server;

import ChatRoom.common.clink.utils.ByteUtils;
import ChatRoom.common.constants.UDPConstants;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;

public class ServerProvider {

    private static Provider PROVIDER_INSTANCE;

    static void start(int port) {
        stop();
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn, port);
        provider.start();
        PROVIDER_INSTANCE = provider;
    }

    static void stop() {
        if (PROVIDER_INSTANCE != null) {
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null;
        }
    }

    private static class Provider extends Thread {
        private final byte[] sn;
        private final int port;
        private boolean done = false;
        private DatagramSocket ds = null;
        final byte[] buffer = new byte[128];

        public Provider(String sn, int port) {
            this.sn = sn.getBytes();
            this.port = port;
        }

        @Override
        public void run() {
            System.out.println("UDPProvider Started...");

            try {
                ds = new DatagramSocket(UDPConstants.PORT_SERVER);

                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

                while (!done) {
                    ds.receive(receivePacket);

                    String clientIp = receivePacket.getAddress().getHostAddress();
                    int clientPort = receivePacket.getPort();
                    int clientDataLen = receivePacket.getLength();
                    byte[] clientData = receivePacket.getData();
                    boolean isValid = clientDataLen >= (UDPConstants.HEADER.length + 2 + 4) && ByteUtils.startsWith(clientData, UDPConstants.HEADER);

                    System.out.println("ServerProvider receive from ip: " + clientIp + ", port: " + clientPort + ", dataValid: " + isValid);
                    if (!isValid)
                        continue;

                    // 解析命令与回送端口
                    int index = UDPConstants.HEADER.length;
                    short cmd = (short) (((clientData[index++] & 0xFF) << 8) | (clientData[index++] & 0xFF));
                    int responsePort = (clientData[index++] & 0xFF) << 24 |
                            (clientData[index++] & 0xFF) << 16 |
                            (clientData[index++] & 0xFF) << 8 |
                            (clientData[index] & 0xFF);

                    // 判断合法性
                    if (cmd == 1 && responsePort > 0) {
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                        byteBuffer.put(UDPConstants.HEADER);
                        byteBuffer.putShort((short) 2);
                        byteBuffer.putInt(port);
                        byteBuffer.put(sn);
                        DatagramPacket responsePacket = new DatagramPacket(buffer, byteBuffer.position(), receivePacket.getAddress(), responsePort);
                        ds.send(responsePacket);
                        System.out.println("ServerProvider response to: " + clientIp + ", port: " + responsePort);
                    } else {
                        System.out.println("ServerProvider receive cmd nonsupport; cmd: " + cmd + ", port: " + responsePort);
                    }
                }
            } catch (Exception e) {
            } finally {
                close();
            }
            System.out.println("UDPProvider Finished...");
        }

        void exit() {
            done = true;
            close();
        }

        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }
    }
}
