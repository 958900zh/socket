package demo04.constants;

public class UDPConstants {
    // 公用头部
    public static byte[] HEADER = new byte[]{6, 6, 6, 6, 6, 6, 6, 6};
    // 服务器固化UDP接收端口
    public static int PORT_SERVER = 50001;
    // 客户端回送端口
    public static int PORT_CLIENT_RESPONSE = 50002;
}
