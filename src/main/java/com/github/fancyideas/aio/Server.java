package com.github.fancyideas.aio;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private final String HOST = "localhost";
    private final int DEFAULT_PORT = 8888;
    private AsynchronousServerSocketChannel serverSocketChannel;

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
                System.out.println("关闭" + closeable);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        //绑定监听端口
        try {
            serverSocketChannel = AsynchronousServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(HOST, DEFAULT_PORT));
            System.out.println("启动服务器，监听端口：" + DEFAULT_PORT);
            serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                @Override
                public void completed(AsynchronousSocketChannel result, Object attachment) {
                    // 如果通道未关闭，继续接收数据
                    if (serverSocketChannel.isOpen()) {
                        serverSocketChannel.accept(null, this);
                    }
                    AsynchronousSocketChannel clientChannel = result;
                    if (clientChannel != null && clientChannel.isOpen()) {
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        Map<String, Object> info = new HashMap<>();
                        info.put("type", "read");
                        info.put("buffer", buffer);
                        clientChannel.read(buffer, info, new CompletionHandler<Integer, Map<String, Object>>() {
                            @Override
                            public void completed(Integer result, Map<String, Object> attachment) {

                            }

                            @Override
                            public void failed(Throwable exc, Map<String, Object> attachment) {

                            }
                        });
                    }
                }

                @Override
                public void failed(Throwable exc, Object attachment) {

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(serverSocketChannel);
        }
    }
}
