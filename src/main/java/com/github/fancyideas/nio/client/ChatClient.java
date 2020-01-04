package com.github.fancyideas.nio.client;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

public class ChatClient {
    private final String HOST = "127.0.0.1";
    private final int PORT = 7777;
    private final String QUIT = "quit";

    private String host;
    private int port;
    private SocketChannel client;
    private ByteBuffer rBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer wBuffer = ByteBuffer.allocate(1024);
    private Selector selector;
    private Charset charset = Charset.forName("UTF-8");

    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public ChatClient() {
        this.host = HOST;
        this.port = PORT;
    }


    public void send(String msg) throws IOException {
        if (msg.isEmpty()) {
            return;
        }
        wBuffer.clear();
        wBuffer.put(charset.encode(msg));
        wBuffer.flip();
        while (wBuffer.hasRemaining()) {
            client.write(wBuffer);
        }
        if (QUIT.equals(msg)) {
            close(selector);
        }
    }

    public String receive(SocketChannel client) throws IOException {
        rBuffer.clear();
        while (client.read(rBuffer) > 0) ;
        rBuffer.flip();
        return String.valueOf(charset.decode(rBuffer));
    }

    public boolean quit(String msg) {
        return QUIT.equals(msg);
    }


    private void start() {
        try {
            client = SocketChannel.open();
            client.configureBlocking(false);
            selector = Selector.open();
            client.register(selector, SelectionKey.OP_CONNECT);
            client.connect(new InetSocketAddress(host, port));
            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key : selectionKeys) {
                    handles(key);
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClosedSelectorException e) {
            System.out.println("客户端退出");
        } finally {
            close(selector);
        }
    }

    private void handles(SelectionKey key) throws IOException {
        // 链接就绪事件 connect 事件
        if (key.isConnectable()) {
            SocketChannel client = (SocketChannel) key.channel();
            // 建立链接完全建立
            if (client.isConnectionPending()) {
                client.finishConnect();
                new Thread(new ChatInputHandler(this)).start();
            }
            client.register(selector, SelectionKey.OP_READ);
        } else if (key.isReadable()) {
            SocketChannel client = (SocketChannel) key.channel();
            String msg = receive(client);
            if (msg.isEmpty()) {
                // 服务器异常
                close(selector);
            } else {
                System.out.println(msg);
            }
        }
        // READ 事件 - 服务器转发消息
    }

    private static void close(Closeable closeable) {
        try {
            if (closeable != null)
                closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.start();
    }
}
