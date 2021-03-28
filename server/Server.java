package server;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.IOException;
import java.net.*;

public class Server {
    private final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() + 1;
    private final ThreadPoolExecutor threadPool;
    private ServerSocket serverSocket;
    private boolean isRunning;
    protected AtomicInteger activeConnection;

    private final int PORT;

    public Server(final int port) throws IOException {
        this.PORT = port;
        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREAD_COUNT);
        serverSocket = new ServerSocket(port);
        activeConnection = new AtomicInteger(0);
    }

    public void start() throws IOException {
        isRunning = true;
        System.out.println("HTTP Server start on port " + PORT);
        while (isRunning) {
            final Socket clientSocket = serverSocket.accept();
            threadPool.submit(new SocketRequestRunnable(clientSocket, this));
        }
    }

    public void stop() {
        isRunning = false;
    }

    public void incActiveConnection() {
        activeConnection.getAndIncrement();
    }

    public void decActiveConnections() {
        activeConnection.getAndDecrement();
    }
}
