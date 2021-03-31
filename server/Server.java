package server;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.*;
import java.nio.file.*;

public class Server {
    private final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() + 1;
    private final ThreadPoolExecutor threadPool;
    private ServerSocket serverSocket;
    private boolean isRunning;
    protected AtomicInteger activeConnection;
    private HashMap<String, DomainInformations> domainMap;

    private final int PORT;

    public Server(final int port) throws IOException {
        this.PORT = port;
        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREAD_COUNT);
        serverSocket = new ServerSocket(port);
        activeConnection = new AtomicInteger(0);
        domainMap = new HashMap<>();
    }
    
    public void start() throws IOException {
        isRunning = true;
        System.out.println("----> HTTP Server start on port " + PORT);
        while (isRunning) {
            final Socket clientSocket = serverSocket.accept();
            threadPool.submit(new SocketRequestRunnable(clientSocket, this));
        }
    }

    /**
     * Stop accpeting requests to this server and starts an orderly shutdown of the thread pool.
     */
    public void stop() {
        isRunning = false;
        threadPool.shutdown();
    }

    /**
     * Increment the number of currently active connections. Thread safe.
     */
    public void incActiveConnection() {
        activeConnection.getAndIncrement();
    }

    /**
     * Decrement the number of currently active connections. Thread safe.
     */
    public void decActiveConnections() {
        activeConnection.getAndDecrement();
    }

    /**
     * Read the vhost.confi file; read the informations about the domains it should serve
     * @return true the setting up was succesful, false otherwise.
     */
    public boolean setUp() {
        String serverRootPath = Path.of("").toAbsolutePath().toString();
        String configFilePath = serverRootPath + "/vhosts.conf";
        File configFile = new File(configFilePath);
        System.out.println("----> Setting up..");
        
        try {
            FileReader freader = new FileReader(configFile);
            BufferedReader breader = new BufferedReader(freader);
            String line;
            String [] values;
            while( (line = breader.readLine()) != null) {
                values = line.split(",");
                if(values.length != 4) {
                    System.err.println("Config file error; bad format");
                    System.err.println("Each line should be of the form <domain>,<entry-point-file>,<member-fullname>,<memeber-email>");
                    return false;
                }
                // Map the domain to the domain informations
                domainMap.put(values[0], new DomainInformations(values[1], values[2], values[3]));
            }

            freader.close();
            breader.close();
        }
        catch(IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Returns whether this server is serving the given domain
     * @param domain the String of the domain name
     * @return true if it is served, false otherwise
     */
    public boolean hasDomain(final String domain) {
        return domainMap.get(domain) != null;
    }

    private class DomainInformations {
        public final String entryPointFile;
        public final String memberEmail;
        public final String memberFullname;

        public DomainInformations(final String entryPointFile, final String memberEmail, final String memberFullname ) {
            this.entryPointFile = entryPointFile;
            this.memberEmail = memberEmail;
            this.memberFullname = memberFullname;
        }
    }
}
