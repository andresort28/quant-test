package com.bitso.repository;

import com.bitso.model.Market;
import com.bitso.model.Order;

import java.util.Collection;
import java.util.Queue;
import java.util.UUID;

/**
 * OrderBook repository to manage OrderBook CRUD operations
 *
 * @author Andres Ortiz
 */
public interface OrderBookRepository {

    /**
     * Find an Order given its id
     *
     * @param id
     * @return
     */
    Order get(UUID id);

    /**
     * Add Order to the OrderBook
     *
     * @param order
     */
    void add(Order order);

    /**
     * Update Order in the OrderBook
     *
     * @param order
     */
    void update(Order order);

    /**
     * Remove Order from the OrderBook
     *
     * @param order
     */
    void remove(Order order);

    /**
     * Fill the Order in the OrderBook
     *
     * @param order
     * @return true is the Order was filled partially or totally, otherwise false
     */
    boolean fillOrder(Order order);

    /**
     * Get a collection of all Orders in the Exchange
     *
     * @return
     */
    Collection<Order> getOrders();

    /**
     * Get the OrderBook given a {@link Market}
     *
     * @return
     */
    OrderBook getOrderBook(Market market);

    /**
     * Get Ask Orders from the OrderBook given a price and a {@link Market}
     *
     * @param market
     * @param price
     * @return
     */
    Queue<Order> getAskOrders(Market market, double price);

    /**
     * Get Bid Orders from the OrderBook given a price and a {@link Market}
     *
     * @param market
     * @param price
     * @return
     */
    Queue<Order> getBidOrders(Market market, double price);
}
