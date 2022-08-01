package com.bitso;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;


/**
 * Exchange Server using non-blocking I/O sockets to receive clients connections and messages from actors of the market
 *
 * @author Andres Ortiz
 */
@Slf4j
public class Exchange {

    private static final int PORT = 8000;
    private static Exchange INSTANCE;

    private Selector selector;

    private Exchange() {
        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(PORT));
            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            log.info("Exchange initialized");
        } catch (IOException e) {
            log.error("Exchange initialization error");
            e.printStackTrace();
        }
    }

    /**
     * Get Singleton instance
     *
     * @return
     */
    public static Exchange getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Exchange();
        }
        return INSTANCE;
    }

    /**
     * Polling to monitor new events to be processed by the Selector
     *
     * @throws IOException
     */
    public void start() throws IOException {
        log.info("Exchange started");
        while (true) {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    register(key);
                }
                if (key.isReadable()) {
                    process(key);
                }
                iterator.remove();
            }
        }
    }

    /**
     * Process connections registering clients to the Selector
     *
     * @param key
     * @throws IOException
     */
    private void register(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        log.info("New client connection");
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    /**
     * Process messages from clients
     *
     * @param key
     * @throws IOException
     */
    private void process(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = clientChannel.read(buffer);
        if (read > 0) {
            String message = new String(buffer.array()).trim();
            log.info("Message received by a client: {}", message);

            //Writing back data
        } else {
            key.cancel();
        }
    }
}
