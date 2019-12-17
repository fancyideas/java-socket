package com.github.fancyideas.bio.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ChatInputHandler implements Runnable {
    private ChatClient chatClient;

    public ChatInputHandler(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String msg = reader.readLine();
                chatClient.send(msg);
                if (chatClient.quit(msg)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
