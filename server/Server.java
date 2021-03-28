package server;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.net.*;
import java.io.*;

public class Server {
    private final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() + 1;
    private final ThreadPoolExecutor threadPool;
    private ServerSocket serverSocket;

    public Server(final int port) {
        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREAD_COUNT);
        serverSocket = new ServerSocket(port);
    }
}
