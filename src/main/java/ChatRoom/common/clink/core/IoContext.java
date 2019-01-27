package ChatRoom.common.clink.core;

import java.io.Closeable;
import java.io.IOException;

public class IoContext implements Closeable {

    private static IoContext INSTANCE;
    private final IoProvider provider;

    private IoContext(IoProvider provider) {
        this.provider = provider;
    }

    public IoProvider getProvider() {
        return provider;
    }

    public static IoContext get() {
        return INSTANCE;
    }

    public static StartBoot setup() {
        return new StartBoot();
    }

    @Override
    public void close() throws IOException {
        provider.close();
    }

    public static class StartBoot {
        private IoProvider provider;

        private StartBoot() { }

        public StartBoot ioProvider(IoProvider provider) {
            this.provider = provider;
            return this;
        }

        public IoContext start() {
            INSTANCE = new IoContext(provider);
            return INSTANCE;
        }
    }

}
