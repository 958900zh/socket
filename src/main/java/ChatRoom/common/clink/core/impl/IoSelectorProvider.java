package ChatRoom.common.clink.core.impl;

import ChatRoom.common.clink.core.IoProvider;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class IoSelectorProvider implements IoProvider {
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final Selector readSelector;
    private final Selector writeSelector;

    private final Map<SelectionKey, Runnable> inputCallbackMap = new HashMap<>();
    private final Map<SelectionKey, Runnable> outputCallbackMap = new HashMap<>();

    private final ExecutorService inputHandlerPool;
    private final ExecutorService outputHandlerPool;

    public IoSelectorProvider() throws IOException {
        readSelector = Selector.open();
        writeSelector = Selector.open();

        inputHandlerPool = Executors.newFixedThreadPool(4,
                new IoProviderThreadFactory("IoProvider-Input-Thread-"));
        outputHandlerPool = Executors.newFixedThreadPool(4,
                new IoProviderThreadFactory("IoProvider-Output-Thread-"));

        // 开始输入输出的监听
        startRead();
        startWrite();
    }

    private void startRead() {
        Thread thread = new Thread("Clink IoSelectorProvider ReadSelector Thread") {
            @Override
            public void run() {
                while (!isClosed.get()) {
                    try {
                        if (readSelector.select() < 0) {

                            continue;
                        }

                        Set<SelectionKey> selectionKeys = readSelector.selectedKeys();
                        for (SelectionKey key : selectionKeys) {
                            handlerSelection(key, SelectionKey.OP_READ, inputCallbackMap, inputHandlerPool);
                        }

                        selectionKeys.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        };

        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    private void startWrite() {
        Thread thread = new Thread("Clink IoSelectorProvider WriteSelector Thread") {
            @Override
            public void run() {
                while (!isClosed.get()) {

                }
            }
        };

        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    @Override
    public boolean registerInput(SocketChannel socketChannel, HandlerInputCallback callback) {
        socketChannel.register(readSelector, SelectionKey.OP_READ);
        return false;
    }

    @Override
    public boolean registerOutput(SocketChannel socketChannel, HandlerOutputCallback callback) {
        return false;
    }

    @Override
    public void unRegisterInput(SocketChannel socketChannel) {

    }

    @Override
    public void unRegisterOutput(SocketChannel socketChannel) {

    }

    @Override
    public void close() throws IOException {

    }

    private void handlerSelection(SelectionKey key, int keyOps,
                                  Map<SelectionKey,Runnable> inputCallbackMap,
                                  ExecutorService pool) {
        // 重点
        key.interestOps(key.readyOps() & ~keyOps);
        Runnable runnable = inputCallbackMap.get(key);

        if (runnable != null && !pool.isShutdown()) {
            pool.submit(runnable);
        }
    }

    static class IoProviderThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        IoProviderThreadFactory(String namePrefix) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
