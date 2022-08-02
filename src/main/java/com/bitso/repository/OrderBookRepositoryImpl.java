package com.bitso.repository;

import com.bitso.model.Market;
import com.bitso.model.Order;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        boolean result = orderBook.add(order);
        log.info("Order {} added to the OrderBook: {}", order.getId(), result);
        orderBooks.put(market, orderBook);
        printOrders();
        printOrderBook(market);
    }

    @Override
    public void update(Order order) {
        Order currentOrder = orders.get(order.getId());
        OrderBook orderBook = orderBooks.get(order.getMarket());
        Order newOrder = orderBook.update(order, currentOrder);
        log.info("Order {} updated in the OrderBook", newOrder);
        orders.replace(order.getId(), newOrder);
        printOrders();
        printOrderBook(order.getMarket());
    }

    @Override
    public void remove(Order order) {
        orders.remove(order.getId());

        OrderBook orderBook = orderBooks.get(order.getMarket());
        boolean result = orderBook.remove(order);
        log.info("Order {} removed from the OrderBook: {}", order.getId(), result);
        printOrders();
        printOrderBook(order.getMarket());
    }

    @Override
    public boolean orderBookExist(Market market) {
        return orderBooks.get(market) != null;
    }

    @Override
    public boolean fillOrder(Order order) {
        OrderBook orderBook = orderBooks.get(order.getMarket());
        List<Order> ordersFilled = orderBook.fillOrder(order);
        if(ordersFilled.isEmpty()) {
            return false;
        }
        for (Order o : ordersFilled) {
            if(o.getAmount() == 0) {
                orders.remove(o.getId());
            } else {
                orders.replace(o.getId(), o);
            }
        }
        return true;
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

    @Override
    public void printOrderBook(Market market) {
        log.debug("--OrderBook Map");
        OrderBook orderBook = orderBooks.get(market);
        log.debug("----Market: {}", market);
        Optional.ofNullable(orderBook).ifPresent(OrderBook::print);
    }

    private void printOrders() {
        log.debug("--Orders Map");
        orders.forEach((id, order) -> log.debug("----{}", order));
    }

    private OrderBookRepositoryImpl() {
    }
}
