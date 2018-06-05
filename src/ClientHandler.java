import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private SocketAddress sAddress;
    private long lastTimeAlive;
    private BigInteger privateExponent, modulus;

    public ClientHandler(Socket socket) {

        clientSocket = socket;
        sAddress = socket.getRemoteSocketAddress();
        privateExponent = new BigInteger(Server.privateExponent);
        modulus = new BigInteger(Server.modulus);

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

            bufferedWriter.write(Server.publicExponent);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            bufferedWriter.write(Server.modulus);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Server.clientSockets.put(clientSocket, bufferedWriter);

            lastTimeAlive = System.currentTimeMillis();
            new Thread(new HeartBeatChecker()).start();

            String message;
            String directedMsg[];
            String decrypted;
            StringBuilder sb;
            while ((message = bufferedReader.readLine()) != null) {
                lastTimeAlive = System.currentTimeMillis();

                if (message.startsWith("#message#")) {

                    if (message.replace("\n", "").
                            trim().equals("")) {

                        continue;
                    }

                    message = message.replace("#message#", "");
                    System.out.println("\nEncrypted: " + message);

                    decrypted = RSA.decrypt(message, privateExponent, modulus);
                    sb = new StringBuilder(decrypted);
                    sb.deleteCharAt(0);

                    System.out.println("Decrypted: " + sb);
                    writeToEveryOne(sb.toString());

                } else if (message.startsWith("#directed_message#")) {

                    if (message.trim().replace("\n", "").
                            equalsIgnoreCase("")) {

                        continue;
                    }

                    message = message.replace("#directed_message#", "");

                    directedMsg = message.split("#separator#");
                    directedMsg[1] = RSA.decrypt(directedMsg[1], privateExponent, modulus);
                    sb = new StringBuilder(directedMsg[1]);
                    sb.deleteCharAt(0);

                    if (directedMsg[1].trim().replace("\n", "").equals("")
                            || directedMsg[1] == null) {

                        continue;
                    }

                    System.out.println("Encrypted: " + message);
                    System.out.println("Decrypted: " + directedMsg[0] + " " + sb.toString());
                    writeToPerson(directedMsg);

                }
            }

        } catch (SocketTimeoutException ex) {
            System.out.println("Client " + sAddress + " disconnected");
            return;
        } catch (IOException e) {

        }
    }

    public synchronized void writeToEveryOne(String message) {

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

    private synchronized void writeToPerson(String[] msg) {

        Map.Entry pair;
        BufferedWriter bw;
        Iterator iterator = Server.clientSockets.entrySet().iterator();
        StringBuilder sb = new StringBuilder(msg[1]);
        sb.deleteCharAt(0);
        msg[1] = sb.toString();

        try {
            while (iterator.hasNext()) {

                pair = (Map.Entry) iterator.next();
                bw = (BufferedWriter) pair.getValue();

                if (((Socket) pair.getKey()).
                        getRemoteSocketAddress().toString().
                        replace("/", "").
                        equals(msg[0])) {

                    bw.write(msg[1]);
                    bw.newLine();
                    bw.flush();

                    return;
                }
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
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
