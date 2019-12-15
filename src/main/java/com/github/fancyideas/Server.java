package com.github.fancyideas;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.MessageFormat;

public class Server {
    public static void main(String[] args) throws IOException {
        final int PORT = 8888;
        final String QUIT = "quit";
        ServerSocket serverSocket = new ServerSocket(PORT);
        String startMsg = MessageFormat.format("启动服务，端口为{0}", PORT);
        System.out.println(startMsg);
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                int port = socket.getPort();
                String clientConnectMsg = MessageFormat.format("客户端[{0}],已经链接", socket.getPort());
                System.out.println(clientConnectMsg);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String msg;
                while ((msg = reader.readLine()) != null) {
                    String clientMsg = MessageFormat.format("客户端[{0}]:{1}", port, msg);
                    System.out.println(clientMsg);
                    writer.write("服务器消息:" + msg + "\n");
                    writer.flush();
                    if (QUIT.equals(msg)) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null)
                serverSocket.close();
            System.out.println("ServerSocket关闭");
        }
    }
}
