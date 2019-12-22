package com.example.echo_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable  {

    private Socket client;
    private DataInputStream in;
    private DataOutputStream out;

    public ClientHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            Retranslator.INSTANCE.send("Client is connected: " + client.getInetAddress());
            in = new DataInputStream(client.getInputStream());
            out = new DataOutputStream(client.getOutputStream());
            out.write("Welcome to Echo-Server, Dear User!\n".getBytes());
            out.flush();

            int byteAmount;
            while (!client.isClosed() && !client.isInputShutdown() && !client.isOutputShutdown()) {
                byteAmount = in.available();
                if (byteAmount > 0) {
                    byte[] bytes = new byte[byteAmount];
                    in.read(bytes);
                    out.write(bytes);
                    out.flush();
                    Retranslator.INSTANCE.send("[to: " + client.getInetAddress() + "] Message is sent, size = " + byteAmount);
                }
            }

            in.close();
            out.close();
            client.close();
            Retranslator.INSTANCE.send("Client is closed: " + client.getInetAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}