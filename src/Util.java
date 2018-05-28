import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Util {

    public static String getLocalIp() {
        String localIp = null;
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while(e.hasMoreElements())  {

                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {

                    InetAddress i = (InetAddress) ee.nextElement();
                    if (i.getHostAddress().startsWith("192.168.")) {
                        localIp = i.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return localIp;
    }
}
