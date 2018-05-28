import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private SocketAddress sAddress;
    private long lastTimeAlive;

    public ClientHandler(Socket socket) {

        clientSocket = socket;
        sAddress = socket.getRemoteSocketAddress();
    }


    @Override
    public void run() {
        try {

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(
                    clientSocket.getInputStream(), Charset.forName("UTF-8"))
            );

            BufferedWriter bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(
                    clientSocket.getOutputStream(), Charset.forName("UTF-8")));

            Server.clientSockets.put(clientSocket, bufferedWriter);
            System.out.println("ELEM: " + Server.clientSockets.size());

            lastTimeAlive = System.currentTimeMillis();
            new Thread(new HeartBeatChecker()).start();

            String message;
            while ((message = bufferedReader.readLine()) != null) {
                lastTimeAlive = System.currentTimeMillis();

                if (message.startsWith("#message#")) {

                    message = message.replace("#message#", "");
                    System.out.println('\n' + message);

                    writeToEveryOne(message);
                }
            }

        } catch (SocketTimeoutException ex) {
            System.out.println("Client " + sAddress + " disconnected");
            return;
        } catch (IOException e) {

        }
    }

    private synchronized void writeToEveryOne(String message) {

        Map.Entry pair;
        BufferedWriter bw;
        Iterator iterator = Server.clientSockets.entrySet().iterator();

        try {
            while (iterator.hasNext()) {

                pair = (Map.Entry) iterator.next();
                bw = (BufferedWriter) pair.getValue();

                bw.write(message);
                bw.newLine();
                bw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class HeartBeatChecker implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {

                    if (System.currentTimeMillis() - lastTimeAlive > 5000) {
                        System.out.println("Client " + sAddress + " disconnected");
                        Server.clientSockets.remove(clientSocket);
                        return;
                    }
                    Thread.sleep(1000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
