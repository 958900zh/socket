package demo03_TCP;

public class Tools {

    public static int byteArraysToInt(byte[] b) {
        return b[3] & 0xFF |
                        (b[2] & 0xFF) << 8 |
                        (b[1] & 0xFF) << 16 |
                        (b[0] & 0xFF) << 24;
    }

    public static byte[] intToByteArray(int i) {
        return new byte[]{
                (byte) ((i >> 24) & 0xFF),
                (byte) ((i >> 16) & 0xFF),
                (byte) ((i >> 8) & 0xFF),
                (byte) (i & 0xFF)
        };
    }
}
