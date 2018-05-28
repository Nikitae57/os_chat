import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Scanner;

public class Client {

    private Socket socket;
    private BufferedReader socketReader = null;
    private String clientName;

    public void startClient() {

        String host;
        int port;

        Scanner sc = new Scanner(System.in);

        System.out.println("Enter your name");
        clientName = sc.nextLine();

        System.out.println("Enter server ip");
        host = sc.nextLine();

        System.out.println("Enter port");
        port = sc.nextInt();

        try {
            this.socket = new Socket(host, port);
            new Thread(new HeartBeat()).start();
        } catch (IOException e) {
            System.out.println("Connection error");
            System.exit(0);
        }

        System.out.println("Connected to " + host + ":" + port);
        System.out.println("Your ip: " + Util.getLocalIp() + '\n');

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = null;
        try {

            socketReader = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), Charset.forName("UTF-8")));

            bw = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream(), Charset.forName("UTF-8")));

            new Thread(new MessageReceiver()).start();

            String message;
            while ((message = br.readLine()) != null) {

                bw.write("#message#" + clientName + ": " + message);
                bw.newLine();
                bw.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
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

                    if (!receivedMessage.startsWith(clientName)) {
                        System.out.println(receivedMessage);
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
