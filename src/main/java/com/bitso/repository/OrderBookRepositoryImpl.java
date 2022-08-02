package com.bitso.repository;

import com.bitso.model.Market;
import com.bitso.model.Order;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementation of {@link OrderBookRepository}
 *
 * @author Andres Ortiz
 */
@Slf4j
public class OrderBookRepositoryImpl implements OrderBookRepository {

    private static OrderBookRepositoryImpl INSTANCE;

    /**
     * Map to store Orders. Store by id to guarantee O(1) search time in the OrderBooks
     */
    private final Map<UUID, Order> orders = new HashMap<>();

    /**
     * Map to store the Market with its OrderBooks
     */
    private final ConcurrentMap<Market, OrderBook> orderBooks = new ConcurrentHashMap<>();

    /**
     * Get Singleton instance
     *
     * @return
     */
    public static OrderBookRepositoryImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OrderBookRepositoryImpl();
        }
        return INSTANCE;
    }

    @Override
    public Order get(UUID id) {
        return orders.get(id);
    }

    @Override
    public void add(Order order) {
        orders.put(order.getId(), order);

        final Market market = order.getMarket();
        OrderBook orderBook = orderBooks.getOrDefault(market, new OrderBook());
        boolean result = orderBook.addOrder(order);
        log.info("Order {} added to the OrderBook: {}", order.getId(), result);
        orderBooks.put(market, orderBook);
        printMaps();
    }

    @Override
    public void update(Order order) {
        order.setModifiedAt(Instant.now());
        orders.replace(order.getId(), order);

        OrderBook orderBook = orderBooks.get(order.getMarket());
        boolean result = orderBook.update(order);
        log.info("Order {} updated in the OrderBook: {}", order.getId(), result);
        printMaps();
    }

    @Override
    public void remove(Order order) {
        orders.remove(order.getId());

        OrderBook orderBook = orderBooks.get(order.getMarket());
        boolean result = orderBook.removeOrder(order);
        log.info("Order {} removed from the OrderBook: {}", order.getId(), result);
        printMaps();
    }

    @Override
    public boolean orderBookExist(Market market) {
        return orderBooks.get(market) != null;
    }

    @Override
    public Queue<Order> getAskOrders(Market market, double price) {
        OrderBook orderBook = orderBooks.get(market);
        if (orderBook != null) {
            return orderBook.getAskOrders().get(price);
        }
        return null;
    }

    @Override
    public Queue<Order> getBidOrders(Market market, double price) {
        OrderBook orderBook = orderBooks.get(market);
        if (orderBook != null) {
            return orderBook.getBidOrders().get(price);
        }
        return null;
    }

    /**
     * Print the Map of Orders, and the Map og OrderBook (LOB) for each Market
     */
    private void printMaps() {
        log.debug("--Orders Maps");
        orders.forEach((id, order) -> log.debug("----{}", order));

        log.debug("--OrderBooks Maps");
        orderBooks.forEach((k, v) -> {
            log.debug("----Market: {}", k);
            v.print();
        });
    }

    private OrderBookRepositoryImpl() {
    }
}
