package com.bitso.repository;

import com.bitso.model.Order;
import com.bitso.model.OrderSide;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * OrderBook representation as data structure
 *
 * @author Andres Ortiz
 */
@Getter
@Slf4j
class OrderBook {

    /**
     * Ask side of the OrderBook (Sell Orders)
     */
    private final ConcurrentMap<Double, PriorityBlockingQueue<Order>> askOrders = new ConcurrentHashMap<>();

    /**
     * Bid side of the OrderBook (Buy Orders)
     */
    private final ConcurrentMap<Double, PriorityBlockingQueue<Order>> bidOrders = new ConcurrentHashMap<>();

    /**
     * Add Order to its corresponding Orders side (Ask/Bid)
     *
     * @param order
     * @return
     */
    protected boolean addOrder(Order order) {
        final double price = order.getPrice();
        PriorityBlockingQueue<Order> defaultQueue = new PriorityBlockingQueue<>(10, Comparator.comparing(Order::getCreatedAt));

        boolean result;
        if (order.getSide() == OrderSide.BUY) {
            PriorityBlockingQueue<Order> queue = bidOrders.getOrDefault(price, defaultQueue);
            result = queue.add(order);
            bidOrders.put(price, queue);
        } else {
            PriorityBlockingQueue<Order> queue = askOrders.getOrDefault(price, defaultQueue);
            result = queue.add(order);
            askOrders.put(price, queue);
        }
        return result;
    }

    /**
     * Remove Order from its Orders side (Ask/Bid)
     *
     * @param order
     * @return
     */
    protected boolean removeOrder(Order order) {
        final double price = order.getPrice();
        boolean result;
        if (order.getSide() == OrderSide.BUY) {
            PriorityBlockingQueue<Order> queue = bidOrders.get(price);
            result = queue.remove(order);
            bidOrders.put(price, queue);
        } else {
            PriorityBlockingQueue<Order> queue = askOrders.get(price);
            result = queue.remove(order);
            askOrders.put(price, queue);
        }
        return result;
    }

    /**
     * Update Order in its OrderBook side (Ask/Bid)
     *
     * @param order
     * @return
     */
    protected Order update(Order order, Order currentOrder) {
        Order newOrder = null;
        if (order.getAmount() > currentOrder.getAmount()) {
            //The createdAt will be new
            newOrder = new Order(order.getId(), order.getMarket(), order.getSide(), order.getPrice(), order.getAmount());
        } else {
            //The createdAt will be the same
            newOrder = order.clone();
        }
        boolean result = removeOrder(currentOrder);
        if (result) {
            result = addOrder(newOrder);
        }
        log.info("Update result of {} : {}", order.getId(), result);
        return newOrder;
    }

    /**
     * Print the entire OrderBook
     */
    protected void print() {
        log.debug("------Ask Orders:");
        printSide(askOrders);

        log.debug("------Bid Orders:");
        printSide(bidOrders);
    }

    /**
     * Print the sorted Orders of a side (Ask or Bid). Only for testing purposes.
     * At time to sort the keys to print them in asc or desc order, the performance will go from O(1) to O(Log(N))
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
}
