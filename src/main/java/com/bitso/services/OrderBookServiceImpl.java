package com.bitso.services;

import com.bitso.model.Market;
import com.bitso.model.Order;
import com.bitso.repository.OrderBook;
import com.bitso.repository.OrderBookRepository;
import com.bitso.repository.OrderBookRepositoryImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;

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
        OrderBook orderBook = orderBookRepository.getOrderBook(market);
        return orderBook != null;
    }

    @Override
    public void printOrderBook(Market market) {
        log.debug("--OrderBook Map");
        OrderBook orderBook = orderBookRepository.getOrderBook(market);
        log.debug("----Market: {}", market);
        log.debug("------Ask Orders:");
        printSide(orderBook.getAskOrders());
        log.debug("------Bid Orders:");
        printSide(orderBook.getBidOrders());
    }

    @Override
    public double getEquilibriumMidMarketPrice(Market market, double halfLife) {
        //Bonus Track (The decay function was not clear in the document neither was the halfLife)
        return 0;
    }

    /**
     * Print the sorted Orders of a side (Ask or Bid)
     * At time to sort the keys to print them in desc order, the time complexity could go from O(1) to O(nlogn) in the worst case
     *
     * @param sideOrders
     */
    private void printSide(ConcurrentMap<Double, PriorityBlockingQueue<Order>> sideOrders) {
        TreeSet<Double> keys = (TreeSet<Double>) new TreeSet<>(sideOrders.keySet()).descendingSet();
        keys.forEach(price -> {
            log.debug("--------Price $ {}:", price);
            sideOrders.get(price).forEach(order -> log.debug("----------{}", order));
        });
    }

    private OrderBookServiceImpl() {
    }
}
