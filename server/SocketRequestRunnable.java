package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import server.message.HTTPRequest;

public class SocketRequestRunnable implements Runnable {
    private final Socket clientSocket;
    private final Server server;
    private HTTPRequest request;

    public SocketRequestRunnable(final Socket clientSocket, final Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.request = new HTTPRequest();
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

        System.out.println("----> Handling clientSocket from " + clientSocket.getInetAddress());
        System.out.println("----> Open Connections: " + server.activeConnection);

        InputStream input = clientSocket.getInputStream();
        String clientAddress = clientSocket.getRemoteSocketAddress().toString();

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        // read request line and headers
        while (!(line = reader.readLine()).equals("")) {
            request.parseAndComputeLine(line);
        }

        // reader.readLine();

        if (request.getMethod().equals("POST") || request.getMethod().equals("PUT")) {
            if (request.getHeaderValue("Content-Length:").isPresent()) {

                while ((line = reader.readLine()) != null) {
                    request.appendBody(line);
                }
            } else {
                // Handle invalid request. Content-Length is missing
            }
        }
        
        request.toStringMio();
        closeConnection();
    }

    private void closeConnection() throws IOException {
        clientSocket.close();
        server.decActiveConnections();
        System.out.println("----> Closed connection");
    }
}
