package com.bitso;


import com.bitso.services.OrderService;
import com.bitso.services.OrderServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import static com.bitso.shared.Config.BUFFER_CAPACITY;
import static com.bitso.shared.Config.PORT;


/**
 * Exchange Server using non-blocking I/O sockets to receive clients connections and messages from actors of the market
 *
 * @author Andres Ortiz
 */
@Slf4j
public class Exchange {

    private static Exchange INSTANCE;

    private Selector selector;

    public static void main(String[] args) throws IOException {
        Exchange exchange = Exchange.getInstance();
        exchange.start();
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
        log.info("Exchange started successfully!");
        while (true) {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    accept(key);
                }
                if (key.isReadable()) {
                    read(key);
                }
                iterator.remove();
            }
        }
    }

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
     * Accept connections and register clients to the Selector
     *
     * @param key
     * @throws IOException
     */
    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        log.info("New client connection");
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    /**
     * Read messages from clients and response
     *
     * @param key
     * @throws IOException
     */
    private void read(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CAPACITY);
        int read = clientChannel.read(buffer);
        if (read > 0) {
            String message = new String(buffer.array()).trim();
            log.info("Raw message received by a client: {}", message);
            boolean result = process(message);
            log.info("Result of the operation: {}", result);

            /*
            --Exchange does not respond to clients (out of the scope of this prototype)--
            --Write back to the client with the result of the operation--
            byte data = result ? (byte)1 : (byte)0;
            ByteBuffer outBuffer = ByteBuffer.wrap(new byte[]{data});
            clientChannel.write(outBuffer);
             */
        } else {
            log.warn("Client shutdown");
            key.cancel();
        }
    }

    private boolean process(String message) {
        return false;
    }
}
