package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import server.message.HTTPRequest;
import server.message.HTTPResponse;

import server.message.StatusCode;

public class SocketRequestRunnable implements Runnable {
    private final Socket clientSocket;
    private final Server server;
    private HTTPRequest request;

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
        // System.out.println("----> Handling clientSocket from " +
        // clientSocket.getInetAddress());

        HTTPResponse response;
        do {
            request = new HTTPRequest(server);
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
            response = new HTTPResponse(server).handleRequest(request);
            output.write(response.toStringMod());
            output.flush();
            log(request, response);

        } while (!response.isLastOne());
        closeConnection();
    }

    private void closeConnection() throws IOException {
        clientSocket.close();
        // System.out.println("----> Closed connection");
    }

    private void log(HTTPRequest req, HTTPResponse res) {
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_GREEN = "\u001B[32m";
        final String ANSI_RESET = "\u001B[0m";

        String color = "";

        if (res.getStatus() == StatusCode.CREATED || res.getStatus() == StatusCode.OK) {
            color = ANSI_GREEN;
        } else {
            color = ANSI_RED;
        }

        System.out.println(color + "[" + res.getStatus() + "] " + ANSI_RESET + req.getMethod() + " " + req.getUrl()
                + " " + req.getVersion());
    }
}
