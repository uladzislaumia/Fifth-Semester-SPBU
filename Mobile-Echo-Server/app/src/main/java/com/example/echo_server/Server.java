package com.example.echo_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {

    private int port;
    private boolean isClosed = false;

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        Retranslator.INSTANCE.send("Server is started");
        try (ServerSocket server = new ServerSocket(port)) {
            while (!server.isClosed() && !isClosed) {
                final Socket client = server.accept();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new ClientHandler(client).run();
                    }
                });
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Retranslator.INSTANCE.send("Server is closed");
    }

    public void close() {
        isClosed = true;
    }
}
