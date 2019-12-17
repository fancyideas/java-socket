package com.github.fancyideas.bio.client;

import java.io.*;
import java.net.Socket;

public class ChatClient {
    private final String HOST = "127.0.0.1";
    private final int PORT = 8888;
    private final String QUIT = "quit";

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

//    public ChatClient(Socket socket, BufferedReader reader, BufferedWriter writer) {
//        this.socket = socket;
//        this.reader = reader;
//        this.writer = writer;
//    }

    public void send(String msg) throws IOException {
        if (socket.isOutputShutdown()) {
            System.out.println("客户端已经关闭了");
        }
        writer.write(msg + "\n");
        writer.flush();
    }

    public String receive() throws IOException {
        if (socket.isInputShutdown()) {
            System.out.println("客户端已经关闭了");
        }
        return reader.readLine();
    }

    public boolean quit(String msg) {
        return QUIT.equals(msg);
    }


    public void start() {
        try {
            socket = new Socket(HOST, PORT);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //TODO 用户输入
            new Thread(new ChatInputHandler(this)).start();
            //TODO 读取转发
            String msg = null;
            while ((msg = receive()) != null) {
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public void close() {
        if (writer != null) {
            try {
                System.out.println("客户端关闭");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.start();
    }
}
