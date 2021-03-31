package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.*;

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

        verifyURL("michelecattaneo.ch", "home.html");


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

    /**
     * 
     * @param host
     * @param url
     * @return
     */
    private boolean verifyURL(String host, String url){
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
