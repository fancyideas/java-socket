package com.github.fancyideas.bio.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private final int PORT = 8888;
    private final String QUIT = "quit";
    private ServerSocket serverSocket;
    private Map<Integer, Writer> connectedCLients;
    // 线程池
    private ExecutorService executorService;

    public ChatServer() {
        executorService = Executors.newFixedThreadPool(10);
        this.connectedCLients = new HashMap<>();
    }

    public synchronized void addClients(Socket socket) throws IOException {
        if (socket == null) {
            System.out.println("未获取到客户端信息,添加失败");
            return;
        }
        int port = socket.getPort();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        connectedCLients.put(port, writer);
        String clientConnectMsg = MessageFormat.format("客户端[{0}]:已经连接到服务器", port);
        System.out.println(clientConnectMsg);
    }

    public synchronized void removeClient(Socket socket) throws IOException {
        if (socket == null) {
            System.out.println("未获取到客户端信息，移除失败");
            return;
        }
        int port = socket.getPort();
        if (!connectedCLients.containsKey(port)) {
            return;
        }
        connectedCLients.get(port).close();
        connectedCLients.remove(port);
        String clientConnectMsg = MessageFormat.format("客户端[{0}]:已经断开", port);
        System.out.println(clientConnectMsg);
    }

    public synchronized void forwardMessage(Socket socket, String msg) throws IOException {
        int currentClientPort = socket.getPort();
        for (Integer clientPort : connectedCLients.keySet()) {
            if (clientPort == currentClientPort) {
                continue;
            }
            Writer writer = connectedCLients.get(clientPort);
            writer.write(msg);
            writer.flush();
        }
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("服务器启动[" + PORT + "]");
            while (true) {
                Socket socket = serverSocket.accept();
                // 伪异步编程模型
                executorService.execute(new ChatHandler(this, socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public void close() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
                System.out.println("close serversocket ");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean readtToQuit(String msg) {
        return QUIT.equals(msg);
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.start();
    }
}
