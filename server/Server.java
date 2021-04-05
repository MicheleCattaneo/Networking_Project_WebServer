package server;

import java.util.LinkedHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
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
    private LinkedHashMap<String, DomainInformations> domainMap;
    public final String serverRootPath;

    private final int PORT;

    public Server(final int port) throws IOException {
        this.PORT = port;
        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREAD_COUNT);
        serverSocket = new ServerSocket(port);
        domainMap = new LinkedHashMap<>();
        serverRootPath = Path.of("").toAbsolutePath().toString();
    }

    /**
     * Start the server on its port. Submit a new task to the threadpool for each
     * socket connection established.
     * 
     */
    public void start() {
        isRunning = true;
        System.out.println("\u001B[35m" + "# HTTP Server start on port " + "\u001B[0m" + PORT);
        while (isRunning) {
            try {
                final Socket clientSocket = serverSocket.accept();
                threadPool.submit(new SocketRequestRunnable(clientSocket, this));
            } catch (IOException e) {
                System.err.println("Socket error");
                e.printStackTrace();
            }
        }
    }

    /**
     * Stop accepting requests to this server and starts an orderly shutdown of the
     * thread pool.
     */
    public void stop() {
        isRunning = false;
        threadPool.shutdown();
    }

    /**
     * Read the vhost.confi file; read the informations about the domains it should
     * serve
     * 
     * @return true the setting up was succesful, false otherwise.
     */
    public boolean setUp() {
        String configFilePath = this.serverRootPath + "/vhosts.conf";
        File configFile = new File(configFilePath);
        System.out.println("\u001B[36m" + "# Setting up..." + "\u001B[0m");

        try {
            FileReader freader = new FileReader(configFile);
            BufferedReader breader = new BufferedReader(freader);
            String line;
            String[] values;
            while ((line = breader.readLine()) != null) {
                values = line.split(",");
                if (values.length != 4) {
                    System.err.println("Config file error; bad format");
                    System.err.println(
                            "Each line should be of the form <domain>,<entry-point-file>,<member-fullname>,<memeber-email>");
                    return false;
                }
                // Map the domain to the domain informations
                domainMap.put(values[0], new DomainInformations(values[1], values[2], values[3]));
            }

            freader.close();
            breader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Returns whether this server is serving the given domain
     * 
     * @param domain the String of the domain name
     * @return true if it is served, false otherwise
     */
    public boolean hasDomain(final String domain) {
        return domainMap.get(domain) != null;
    }

    /**
     * Retrieve the default host, meaning the first host read in the vhosts.conf
     * file.
     * 
     * @return the default host name
     */
    public String getDefaultHost() {
        return domainMap.keySet().iterator().next();
    }

    /**
     * Get a shallow copy of the Domain mapping of the server.
     * 
     * @return the copy of the Domain mapping
     */
    public Object getDomainsInformations() {
        return domainMap.clone();
    }

    /**
     * Return the entry point of a Host.
     * 
     * @param host the host
     * @return the entry point for that host
     */
    public String getEntryPoint(final String host) {
        DomainInformations d = domainMap.get(host);
        if (d == null) {
            return null;
        }
        return d.entryPointFile;
    }

    /**
     * Inner Class used as a container for the informations about the different
     * hosts of this server.
     */
    public class DomainInformations {
        public final String entryPointFile;
        public final String memberEmail;
        public final String memberFullname;

        public DomainInformations(final String entryPointFile, final String memberFullname, final String memberEmail) {
            this.entryPointFile = entryPointFile;
            this.memberEmail = memberEmail;
            this.memberFullname = memberFullname;
        }
    }

}
