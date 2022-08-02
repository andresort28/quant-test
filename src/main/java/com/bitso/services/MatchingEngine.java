package com.bitso.services;

import com.bitso.model.Order;
import com.bitso.repository.OrderBookRepository;
import com.bitso.repository.OrderBookRepositoryImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * Matching Engine in charge to execute trades and update the LOB
 *
 * @author Andres Ortiz
 */
@Slf4j
public class MatchingEngine {

    private static MatchingEngine INSTANCE;
    private final OrderBookRepository orderBookRepository = OrderBookRepositoryImpl.getInstance();

    /**
     * Get Singleton instance
     *
     * @return
     */
    public static MatchingEngine getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MatchingEngine();
        }
        return INSTANCE;
    }

    /**
     * Execute the trade trying to fill the Order in the OrderBook
     * @param order
     * @return
     */
    public synchronized boolean executeTrade(Order order) {
        log.info("Executing new incoming Trade: {}", order);
        boolean result = orderBookRepository.fillOrder(order);
        log.info("Result of filling of {}: {}", order.getId(), result);
        return result;
    }
}
