package server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import jdk.javadoc.internal.doclets.formats.html.SourceToHTMLConverter;
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


            if (!request.isMalformed() && request.shouldHaveBody()) {
                
                // byte[] byteArray = input.readNBytes(request.getContentLength());
                // byte[] byteArray = input.readAllBytes();
                DataInputStream inFromClient = new DataInputStream(input);
                byte[] byteArray = new byte[2048];
                inFromClient.readFully(byteArray, 0, request.getContentLength());
                request.setBody(byteArray);
                for (int i = 0; i < 10; i++) {
                    System.out.println(byteArray[i]);
                }
                System.out.println(byteArray.length);
                // int byteSum = 0;
                // while (byteSum < request.getContentLength()) {
                //     line = reader.readLine();
                //     request.appendBody(line);
                //     byteSum += line.getBytes().length;
                // }
                // if (byteSum != request.getContentLength()) {
                //     request.setMalformed();
                // }
                System.out.println("hi");

            }
            response = new HTTPResponse(server).handleRequest(request);
            output.write(response.toStringMod());
            output.flush();
            log(request, response);

        } while (!response.isLastOne());
        closeConnection();
    }

    /**
     * Close this socket connection.
     * 
     * @throws IOException when the closing encounters an error.
     */
    private void closeConnection() throws IOException {
        clientSocket.close();
        System.out.println("----> Closed connection");
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
