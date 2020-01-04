package com.github.fancyideas.nio.server;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private final int PORT = 8888;
    private final String QUIT = "quit";
    private final int BUFFER = 1024;
    // NIO三兄弟
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);
    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);
    private Charset charset = Charset.forName("UTF-8");
    private int port;

    public ChatServer(int port) {
        this.port = port;
    }

    public ChatServer() {
        this.port = PORT;
    }

    private void start() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("启动服务器，监听端口：" + port);
            while (true) {
                selector.select();
                // 已经触发的keys
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey selectionKey : selectionKeys) {
                    handles(selectionKey);
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(selector);
        }
    }

    private void handles(SelectionKey selectionKey) throws IOException {
        if (selectionKey.isAcceptable()) {
            ServerSocketChannel serverChannel = (ServerSocketChannel) selectionKey.channel();
            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
            System.out.println("客户端已经链接:" + getClientName(clientChannel));
        } else if (selectionKey.isReadable()) {
            SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
            String fwdMsg = receive(clientChannel);
            if (fwdMsg.isEmpty()) {
                selectionKey.cancel();
                selector.wakeup();
            } else {
                forwardMessage(clientChannel, fwdMsg);
                if (readToQuit(fwdMsg)) {
                    selectionKey.cancel();
                    selector.wakeup();
                    System.out.println("客户端已经断开:" + getClientName(clientChannel));
                }
            }
        }
    }

    private void forwardMessage(SocketChannel clientChannel, String fwdMsg) throws IOException {
        // 所有注册的keys
        for (SelectionKey key : selector.keys()) {
            Channel connectedClient = key.channel();
            if (connectedClient instanceof ServerSocketChannel) {
                continue;
            }
            // valid是正常的状态
            if (key.isValid() && !clientChannel.equals(connectedClient)) {
                wBuffer.clear();
                wBuffer.put(charset.encode(getClientName(clientChannel) + fwdMsg));
                wBuffer.flip();
                while (wBuffer.hasRemaining()) {
                    ((SocketChannel) connectedClient).write(wBuffer);
                }
            }
        }
    }

    private String getClientName(SocketChannel socketChannel) {
        return socketChannel.socket().getPort() + "";
    }

    private String receive(SocketChannel clientChannel) throws IOException {
        rBuffer.clear();
        while (clientChannel.read(rBuffer) > 0) ;
        rBuffer.flip();
        return String.valueOf(charset.decode(rBuffer));
    }


    public void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
                System.out.println("close serversocket ");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean readToQuit(String msg) {
        return QUIT.equals(msg);
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer(7777);
        chatServer.start();
    }
}
