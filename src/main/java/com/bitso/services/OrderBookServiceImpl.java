package com.bitso.services;

import com.bitso.model.Market;
import com.bitso.model.Order;
import com.bitso.repository.OrderBookRepository;
import com.bitso.repository.OrderBookRepositoryImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;

/**
 * Implementation of {@link OrderBookService}
 *
 * @author Andres Ortiz
 */
@Slf4j
public class OrderBookServiceImpl implements OrderBookService {

    private static OrderBookServiceImpl INSTANCE;
    private final OrderBookRepository orderBookRepository = OrderBookRepositoryImpl.getInstance();

    /**
     * Get Singleton instance
     *
     * @return
     */
    public static OrderBookService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OrderBookServiceImpl();
        }
        return INSTANCE;
    }

    @Override
    public Queue<Order> getBuyOrders(Market market, double price) {
        return orderBookRepository.getBidOrders(market, price);
    }

    @Override
    public Queue<Order> getSellOrders(Market market, double price) {
        return orderBookRepository.getAskOrders(market, price);
    }

    @Override
    public boolean orderBookExist(Market market) {
        return orderBookRepository.orderBookExist(market);
    }

    @Override
    public void printOrderBook(Market market) {
        orderBookRepository.printOrderBook(market);
    }

    @Override
    public double getEquilibriumMidMarketPrice(Market market, double halfLife) {
        //TODO Bonus Track
        return 0;
    }

    private OrderBookServiceImpl() {
    }
}
