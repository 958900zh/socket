package ChatRoom.common.clink.core.impl;

import ChatRoom.common.clink.core.IoArgs;
import ChatRoom.common.clink.core.IoProvider;
import ChatRoom.common.clink.core.Receiver;
import ChatRoom.common.clink.core.Sender;
import ChatRoom.common.clink.utils.CloseUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketChannelAdaptor implements Sender, Receiver, Closeable {
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final SocketChannel channel;
    private final IoProvider ioProvider;
    private final OnChannelStatusChangedListener listener;

    private IoArgs.IoArgsEventListener receiveIoEventListener;
    private IoArgs.IoArgsEventListener sendIoEventListener;

    public SocketChannelAdaptor(SocketChannel channel, IoProvider ioProvider, OnChannelStatusChangedListener listener) throws IOException {
        this.channel = channel;
        this.ioProvider = ioProvider;
        this.listener = listener;

        this.channel.configureBlocking(false);
    }

    @Override
    public boolean receiveAsync(IoArgs.IoArgsEventListener listener) throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current channel is closed");
        }

        receiveIoEventListener = listener;
        return ioProvider.registerInput(channel, inputCallback);
    }

    @Override
    public boolean sendAsync(IoArgs args, IoArgs.IoArgsEventListener listener) throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current channel is closed");
        }
        sendIoEventListener = listener;
        // 当前发送的数据附加到回调中
        outputCallback.setAttach(args);
        return ioProvider.registerOutput(channel, outputCallback);
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            // 解除注册回调
            ioProvider.unRegisterInput(channel);
            ioProvider.unRegisterOutput(channel);
            // 关闭
            CloseUtils.close(channel);
            // 回调当前channel已关闭
            listener.onChannelChanged(channel);
        }
    }

    private final IoProvider.HandlerInputCallback inputCallback = new IoProvider.HandlerInputCallback() {
        @Override
        protected void canProviderInput() {
            if (isClosed.get())
                return;

            IoArgs args = new IoArgs();
            IoArgs.IoArgsEventListener listener = SocketChannelAdaptor.this.receiveIoEventListener;
            if (listener != null) {
                listener.onStarted(args);
            }

            // 具体的读取操作
            try {
                // 具体的读取操作
                if (args.read(channel) > 0 && listener != null) {
                    // 读取完成回调
                    listener.onCompleted(args);
                } else {
                    throw new IOException("Cannot read any data!");
                }
            } catch (IOException e) {
                CloseUtils.close(SocketChannelAdaptor.this);
            }
        }
    };

    private final IoProvider.HandlerOutputCallback outputCallback = new IoProvider.HandlerOutputCallback() {
        @Override
        protected void canProviderOutput(Object attach) {
            if (isClosed.get())
                return;
            // TODO
            sendIoEventListener.onCompleted(null);
        }
    };

    public interface OnChannelStatusChangedListener {
        void onChannelChanged(SocketChannel channel);
    }
}
