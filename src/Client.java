import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Client {

    private Socket socket;
    private BufferedReader socketReader = null;
    private String sAddress;
    private BigInteger publicExponent, modulus;

    public void startClient() {

        String host = null;
        int port = 0;

        try {

            Scanner sc = new Scanner(System.in);

            System.out.println("Enter server ip");
            host = sc.nextLine();

            System.out.println("Enter port");
            port = sc.nextInt();

            this.socket = new Socket(host, port);
            sAddress = socket.getLocalSocketAddress().toString();
            new Thread(new HeartBeat()).start();

        } catch (IOException e) {

            System.out.println("Connection error");
            System.exit(0);

        } catch (InputMismatchException ex) {

            System.out.println("Incorrect input");
        }

        System.out.println("\033[H\033[2J");
        System.out.flush();
        System.out.println("Connected to " + host + ":" + port);
        System.out.println("Your address: " + sAddress + '\n');

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = null;
        try {

            socketReader = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), Charset.forName("UTF-8")));

            publicExponent = new BigInteger(socketReader.readLine());
            modulus = new BigInteger(socketReader.readLine());

            bw = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream(), Charset.forName("UTF-8")));

            String encryptedFirstMsg = RSA.encrypt("aClient "
                    + sAddress + " connected", publicExponent, modulus);

            bw.write("#message#" + encryptedFirstMsg);
            bw.newLine();
            bw.flush();

            new Thread(new MessageReceiver()).start();

            String message;
            String tmp[];
            String words[];
            String encrypted;
            while ((message = br.readLine()) != null) {

                if (message.startsWith("SENDTO")) {

                    message = message.replace("SENDTO", "").trim();

                    words = message.split("\\s+");
                    if (message.replace("\n", "").isEmpty()
                            || words.length != 2) {

                        continue;
                    }

                    tmp = message.split(" ", 2);
                    encrypted = RSA.encrypt('a' + tmp[1], publicExponent, modulus);
                    bw.write("#directed_message#" + tmp[0] + "#separator#" + encrypted);

                } else {

                    if (!message.replace("\n", "").trim().equals("")) {

                        encrypted = RSA.encrypt('a' + sAddress + " writes: "
                                + message, publicExponent, modulus);

                        bw.write("#message#" + encrypted);
                        System.out.println("Encrypted: " + encrypted);
                    }
                }

                bw.newLine();
                bw.flush();
            }

        } catch (Exception e) {
            System.out.println("Something gone wrong");

        }  finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class HeartBeat implements Runnable {


        @Override
        public void run() {
            try {

                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                while (true) {
                    dos.writeUTF("#alive#\n");
                    dos.flush();
                    Thread.sleep(1000);
                }
            } catch (IOException e) {
                System.out.println("Program terminated");
                System.exit(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class MessageReceiver implements Runnable {

        @Override
        public void run() {
            try {

                String receivedMessage;
                while ((receivedMessage = socketReader.readLine()) != null) {

                    if (receivedMessage.contains(sAddress)) {
                        continue;
                    }

                    System.out.println('\n' + receivedMessage);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
