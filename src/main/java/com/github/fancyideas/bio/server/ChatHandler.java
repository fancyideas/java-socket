package com.github.fancyideas.bio.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.MessageFormat;

public class ChatHandler implements Runnable {
    private ChatServer chatServer;
    private Socket socket;

    public ChatHandler(ChatServer chatServer, Socket socket) {
        this.chatServer = chatServer;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            chatServer.addClients(socket);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String currentClientMsg;
            while ((currentClientMsg = reader.readLine()) != null) {
                String clientMsg = MessageFormat.format("客户端[{0}]:{1} {2}",
                        socket.getPort(),
                        currentClientMsg,
                        "\n");
                System.out.printf(clientMsg);
                chatServer.forwardMessage(socket, clientMsg);
                if (chatServer.readtToQuit(currentClientMsg)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                chatServer.removeClient(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
