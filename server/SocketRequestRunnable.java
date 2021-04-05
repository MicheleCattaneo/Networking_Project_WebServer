package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import server.message.HTTPRequest;
import server.message.HTTPResponse;

public class SocketRequestRunnable implements Runnable {
    private final Socket clientSocket;
    private final Server server;
    private HTTPRequest request;

    public SocketRequestRunnable(final Socket clientSocket, final Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.request = new HTTPRequest(server);
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
        System.out.println("----> Handling clientSocket from " + clientSocket.getInetAddress());

        HTTPResponse response;
        do {
            response = new HTTPResponse(server);
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;

            // read request line and headers
            line = reader.readLine(); // read request line
            if (request.parseRequestLine(line)) {
                while (!(line = reader.readLine()).equals("")) {
                    request.parseAndComputeLine(line);
                }
            }

            if (request.shouldHaveBody()) {
                while ((line = reader.readLine()) != null) {
                    request.appendBody(line);
                }

            }

            request.toStringMio();
            response = new HTTPResponse(server).handleRequest(request);
            output.write(response.toStringMod());
            output.flush();

        } while (!response.isLastOne());
        closeConnection();
    }

    private void closeConnection() throws IOException {
        clientSocket.close();
        System.out.println("----> Closed connection");
    }
}
