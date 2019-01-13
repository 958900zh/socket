package demo04.clink.utils;

public class ByteUtils {

    public static boolean startsWith(byte[] source, byte[] target) {
        int len = target.length;
        if (source.length < len)
            return false;
        for (int i = 0; i < len; i++) {
            if (source[i] != target[i])
                return false;
        }

        return true;
    }
}
