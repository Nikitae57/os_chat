import java.io.*;
import java.security.*;
import java.util.*;
import java.net.*;

public class Server {

    public static Map<Socket, BufferedWriter> clientSockets;

    public void startServer() {

        clientSockets = new HashMap<Socket, BufferedWriter>();

        System.out.println("Enter port");
        int port = new Scanner(System.in).nextInt();
        System.out.println("Server started");

        String localIp = Util.getLocalIp();
        System.out.println("Clients can connect to " + localIp + ":" + port + '\n');

        try {

            ServerSocket serverSocket = new ServerSocket(port);
            Socket socket;

            ClientHandler clientHandler;
            while (true) {

                socket = serverSocket.accept();
                clientHandler = new ClientHandler(socket);

                System.out.println("\nClient " + socket.getRemoteSocketAddress() + " connected\n");

                new Thread(clientHandler).start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
