package com.bitso.repository;

import com.bitso.model.Market;
import com.bitso.model.Order;

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
     * Exist an OrderBook of a given {@link Market}
     *
     * @param market
     * @return
     */
    boolean orderBookExist(Market market);

    /**
     * Fill the Order in the OrderBook
     *
     * @param order
     * @return true is the Order was filled partially or totally, otherwise false
     */
    boolean fillOrder(Order order);

    /**
     * Get Ask Orders from the OrderBook of a specific price of a certain {@link Market}
     *
     * @param market
     * @param price
     * @return
     */
    Queue<Order> getAskOrders(Market market, double price);

    /**
     * Get Bid Orders from the OrderBook of a specific price of a certain {@link Market}
     *
     * @param market
     * @param price
     * @return
     */
    Queue<Order> getBidOrders(Market market, double price);

    /**
     * Print the OrderBook of a given {@link Market}
     * @param market
     */
    void printOrderBook(Market market);
}
