package com.bitso.services;

import com.bitso.model.Market;
import com.bitso.model.Order;

import java.util.Queue;

/**
 * OrderBook service to manage OrderBook operations
 *
 * @author Andres Ortiz
 */
public interface OrderBookService {

    /**
     * Get BUY Orders from the OrderBook of a specific price of a certain {@link Market}
     *
     * @param market
     * @param price
     * @return
     */
    Queue<Order> getBuyOrders(Market market, double price);

    /**
     * Get SELL Orders from the OrderBook of a specific price of a certain {@link Market}
     *
     * @param market
     * @param price
     * @return
     */
    Queue<Order> getSellOrders(Market market, double price);

    /**
     * Exist an OrderBook of a given {@link Market}
     * @param market
     * @return
     */
    boolean orderBookExist(Market market);

    /**
     * Calculate the equilibrium mid-market price (EP) which is the equilibrium price of cumulative discounted total
     * volume functions of bid and ask side of a {@link Market} given a {@code halfLife}
     *
     * @param market
     * @param halfLife
     * @return
     */
    double getEquilibriumMidMarketPrice(Market market, double halfLife);
}
