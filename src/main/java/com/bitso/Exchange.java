package com.bitso;


import com.bitso.exception.MessageNotSupportedException;
import com.bitso.exception.OrderNotFoundException;
import com.bitso.model.Market;
import com.bitso.model.Message;
import com.bitso.model.Order;
import com.bitso.services.MatchingEngine;
import com.bitso.services.OrderBookService;
import com.bitso.services.OrderBookServiceImpl;
import com.bitso.services.OrderService;
import com.bitso.services.OrderServiceImpl;
import com.bitso.shared.Decoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.time.Instant;
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
    private final OrderService orderService = OrderServiceImpl.getInstance();
    private final OrderBookService orderBookService = OrderBookServiceImpl.getInstance();
    private final MatchingEngine matchingEngine = MatchingEngine.getInstance();

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
        log.info("\n");
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
            final Instant start = Instant.now();
            String message = new String(buffer.array()).trim();
            log.info("Raw message received: {}", message);
            process(message);
            final Instant stop = Instant.now();
            log.info("Operation finished. Duration: {}", Duration.between(start, stop));

            /*
            //--Exchange does not respond to clients (out of the scope of this prototype)--
            //--Write back to the client with the result of the operation--
            boolean result = process(message);
            byte data = result ? (byte)1 : (byte)0;
            ByteBuffer outBuffer = ByteBuffer.wrap(new byte[]{data});
            clientChannel.write(outBuffer);
             */
        } else {
            log.warn("Client shutdown");
            key.cancel();
        }
    }

    /**
     * Process message received by clients
     *
     * @param message
     */
    protected void process(String message) {
        try {
            Message msg = Decoder.decode(message);
            log.info("Decoded message: {}", msg);
            switch (msg.getMessageType()) {
                case ADD -> {
                    Order order = orderService.parseOrder(msg);
                    log.info("Adding new Order {}", order.getId());
                    orderService.addOrder(order);
                    print(msg.getMarket());
                    matchingEngine.executeTrade(order);
                }
                case DELETE -> {
                    log.info("Deleting Order {}", msg.getOrderId());
                    orderService.deleteOrder(msg.getOrderId());
                    print(msg.getMarket());
                }
                case MODIFY -> {
                    log.info("Modifying Order {}, New Amount {}", msg.getOrderId(), msg.getAmount());
                    orderService.modifyOrder(msg.getOrderId(), msg.getAmount());
                    print(msg.getMarket());
                }
                case PRINT -> {
                    log.info("Print OrderBook {}", msg.getMarket());
                    print(msg.getMarket());

                    //Testing Only: Use of PRINT Message to calculate the equilibrium mid-market price (EP)
                    log.debug("Equilibrium mid-market price (EP): {}", orderBookService.getEquilibriumMidMarketPrice(msg.getMarket(), 0.5));
                }
            }
        } catch (MessageNotSupportedException e) {
            log.error("Error decoding the message", e);
        } catch (OrderNotFoundException e) {
            log.error("Error deleting or modifying an Order", e);
        }
    }

    /**
     * Print Orders and OrderBook given a {@link Market}
     *
     * @param market
     */
    private void print(Market market) {
        orderService.printOrders();
        orderBookService.printOrderBook(market);
    }
}
