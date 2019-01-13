package demo04.client;

import demo04.bean.ServerInfo;
import demo04.constants.UDPConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientSearcher {

    private static final int LISTEN_PORT = UDPConstants.PORT_CLIENT_RESPONSE;

    public static ServerInfo searchServer(int timeout) {
        System.out.println("UDPSearcher Started...");

        CountDownLatch receiveLatch = new CountDownLatch(1);
        Listener listener = null;

        try {
            listener = listen(receiveLatch);
            sendBroadcast();
            receiveLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("UDPSearcher Finished...");

        if (listener == null)
            return null;

        List<ServerInfo> devices = listener.getServerAndClose();
        if (devices.size() > 0)
            return devices.get(0);

        return null;
    }

    private static void sendBroadcast() throws SocketException {
        System.out.println("UDPSearcher sendBroadcast started...");

        DatagramSocket ds = new DatagramSocket();

        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        byteBuffer.put(UDPConstants.HEADER);
        byteBuffer.putShort((short) 1);
        byteBuffer.putInt(LISTEN_PORT);

//        DatagramPacket requestPack
    }

    private static Listener listen(CountDownLatch receiveLatch) throws InterruptedException {
        System.out.println("UDPSearcher start listen...");
        CountDownLatch startDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, startDownLatch, receiveLatch);
        listener.start();
        startDownLatch.await();
        return listener;
    }

    private static class Listener extends Thread {

        private final int listenPort;
        private final CountDownLatch startDownLatch;
        private final CountDownLatch receiveLatch;
        private final List<ServerInfo> serverInfoList = new ArrayList<>();
        private final byte[] buffer = new byte[128];
        private boolean done = false;
        private DatagramSocket ds = null;
        private final int minLen = UDPConstants.HEADER.length + 2 + 4;

        public Listener(int listenPort, CountDownLatch startDownLatch, CountDownLatch receiveLatch) {
            this.listenPort = listenPort;
            this.startDownLatch = startDownLatch;
            this.receiveLatch = receiveLatch;
        }

        @Override
        public void run() {
            startDownLatch.countDown();
            try {
                ds = new DatagramSocket(listenPort);

                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

                while (!done) {
                    ds.receive(receivePacket);
                    // todo
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }
        }

        public List<ServerInfo> getServerAndClose() {
            // todo
            return null;
        }

        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }
    }
}