package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.*;

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
        server.incActiveConnection();

        System.out.println("----> Handling clientSocket from " + clientSocket.getInetAddress());
        System.out.println("----> Open Connections: " + server.activeConnection);

        HTTPResponse response;
        do {
            response = new HTTPResponse(server);
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();
            // String clientAddress = clientSocket.getRemoteSocketAddress().toString();

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
            // TODO: generate response
            request.toStringMio();
            response = new HTTPResponse(server).handleRequest(request);
            // System.out.println(response.toString());
            output.write(response.toString().getBytes());
            output.flush();

        } while (!response.isLastOne());
        closeConnection();
    }

    /**
     * 
     * @param host
     * @param url
     * @return
     */
    private boolean verifyURL(String host, String url) {
        String serverRootPath = Path.of("").toAbsolutePath().toString();
        // alternatively: System.getProperty("user.dir");

        // assert url does not get back into the file system or nulls are given
        if (host == null || url == null || url.contains("../")) {
            return false;
        }

        // assert domain is served
        if (!server.hasDomain(host)) {
            System.err.println("Unknown host");
            return false;
        }

        // Assert host is valid
        String hostRootPath = serverRootPath + "/" + host;
        File hostRootFile = new File(hostRootPath);
        if (!hostRootFile.exists()) {
            System.err.println("invalid host");
            return false;
        }

        // Assert requested url is a file in the host
        // TODO: check if the requested object is the folder itself?
        String objectPath = hostRootPath + "/" + url;
        File objectFile = new File(objectPath);
        if (!objectFile.exists()) {
            System.err.println("invalid object");
            return false;
        }

        System.out.println("all good");
        return true;
    }

    private void closeConnection() throws IOException {
        clientSocket.close();
        server.decActiveConnections();
        System.out.println("----> Closed connection");
    }
}
