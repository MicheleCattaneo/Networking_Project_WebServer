package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class SocketRequestRunnable implements Runnable {
    private final Socket clientSocket;
    private final Server server;

    public SocketRequestRunnable(final Socket clientSocket, final Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            handleHTTPRequest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleHTTPRequest() throws IOException {
        server.incActiveConnection();

        System.out.println("Handling clientSocket from " + clientSocket.getInetAddress());
        System.out.println("Open Connections: " + server.activeConnection);

        InputStream input = clientSocket.getInputStream();
        String clientAddress = clientSocket.getRemoteSocketAddress().toString();

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        while (!(line = reader.readLine()).equals(""))
            System.out.println(clientAddress + ": " + line);

        closeConnection();
    }

    private void closeConnection() throws IOException {
        clientSocket.close();
        server.decActiveConnections();
        System.out.println("Closed connection");
    }
}
