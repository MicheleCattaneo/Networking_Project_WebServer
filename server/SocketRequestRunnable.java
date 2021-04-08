package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    /**
     * Handle the HTTP request.
     */
    private void handleHTTPRequest() throws IOException {
        // Uncomment to see client's address
        // System.out.println("----> Handling clientSocket from " +
        // clientSocket.getInetAddress());

        HTTPResponse response;
        do {
            request = new HTTPRequest(server);
            response = new HTTPResponse(server);
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            String line;
            // read request line and headers
            line = readLineOfBytes(input); // read request line
            if (request.parseRequestLine(line)) {
                while (!(line = readLineOfBytes(input)).equals("")) {
                    request.parseAndComputeLine(line);
                }
            }

            if (!request.isMalformed() && request.shouldHaveBody()) {
                
                byte[] byteArray = input.readNBytes(request.getContentLength());
                request.setBody(byteArray);

            }
            response = new HTTPResponse(server).handleRequest(request);
            output.write(response.toStringMod());
            output.flush();
            log(request, response);

        } while (!response.isLastOne());
        closeConnection();
    }

    private String readLineOfBytes(InputStream input) throws IOException {
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        int b = input.read();
        int c = input.read();
        while (!(b == '\r' && c == '\n')) {
            bStream.write(b);
            b = c;
            c = input.read();
        }
        System.out.println(bStream.toString());
        return bStream.toString();
    }

    /**
     * Close this socket connection.
     * 
     * @throws IOException when the closing encounters an error.
     */
    private void closeConnection() throws IOException {
        clientSocket.close();
        // System.out.println("----> Closed connection");
    }

    /**
     * Print a log for requests with Status Code, method and HTTP version.
     * 
     * @param req the request
     * @param res the response
     */
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
