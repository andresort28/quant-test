package com.bitso.repository;

import com.bitso.model.Order;
import com.bitso.model.OrderSide;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
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
    protected boolean add(Order order) {
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
    protected boolean remove(Order order) {
        final double price = order.getPrice();
        boolean result;
        if (order.getSide() == OrderSide.BUY) {
            PriorityBlockingQueue<Order> queue = bidOrders.get(price);
            result = queue.remove(order);
            bidOrders.replace(price, queue);
        } else {
            PriorityBlockingQueue<Order> queue = askOrders.get(price);
            result = queue.remove(order);
            askOrders.replace(price, queue);
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
        Order newOrder;
        if (order.getAmount() > currentOrder.getAmount()) {
            newOrder = new Order(order.getId(), order.getMarket(), order.getSide(), order.getPrice(), order.getAmount());
        } else {
            newOrder = order;
        }
        boolean result = remove(currentOrder);
        if (result) {
            result = add(newOrder);
        }
        log.info("Update result of {} : {}", order.getId(), result);
        return newOrder;
    }

    /**
     * Fill the Order looking for its counterpart on the other side of the market
     *
     * @param order
     * @return List with all Order filled
     */
    public List<Order> fillOrder(Order order) {
        final double price = order.getPrice();
        final UUID orderId = order.getId();
        List<Order> filled = new ArrayList<>();

        if (order.getSide() == OrderSide.BUY) {
            log.info("Looking Sell Orders (Ask Side) to fill: {}", order);
            PriorityBlockingQueue<Order> queue = askOrders.get(price);
            if (queue != null) {
                while (!queue.isEmpty() && order.getAmount() > 0) {
                    Order headOrder = queue.peek();
                    final double availableAmount = headOrder.getAmount();
                    final double amountToFill = order.getAmount();
                    if (availableAmount == amountToFill) {
                        log.info("Sell Order fully filled: {}", headOrder);
                        queue.remove();
                        log.info("Sell Order {} was removed from the OrderBook", headOrder.getId());
                        order.setAmount(0);
                        headOrder.setAmount(0);
                        filled.add(headOrder);
                        askOrders.replace(price, queue);
                        log.info("Trade {} was fully filled", orderId);
                        remove(order);
                        log.info("Trade {} was removed from the OrderBook", orderId);
                    } else if (availableAmount > amountToFill) {
                        final double remaining = availableAmount - amountToFill;
                        log.info("Sell Order partially filled: {}, Remaining Amount {}", headOrder, remaining);
                        order.setAmount(0);
                        Order orderFilled = headOrder.clone();
                        orderFilled.setAmount(remaining);
                        filled.add(headOrder);
                        update(orderFilled, headOrder);
                        log.info("Trade {} was fully filled", orderId);
                        remove(order);
                        log.info("Trade {} was removed from the OrderBook", orderId);
                    } else {
                        // availableAmount < amountToFill
                        log.info("Sell Order fully filled: {}", headOrder);
                        queue.remove();
                        log.info("Sell Order {} was removed from the OrderBook", headOrder.getId());
                        final double remaining = amountToFill - availableAmount;
                        order.setAmount(remaining);
                        headOrder.setAmount(0);
                        filled.add(headOrder);
                        askOrders.replace(price, queue);
                        log.info("Trade {} was partially filled {}, Remaining Amount {}", orderId, remaining);
                    }
                }
            } else {
                log.info("There is not any Sell Order at price $ {} to fill the Order {}", price, orderId);
            }
        } else {
            log.info("Looking Buy Orders (Bid Side) to fill: {}", order);
            PriorityBlockingQueue<Order> queue = bidOrders.get(price);
            if (queue != null) {
                while (!queue.isEmpty() && order.getAmount() > 0) {
                    Order headOrder = queue.peek();
                    final double availableAmount = headOrder.getAmount();
                    final double amountToFill = order.getAmount();
                    if (availableAmount == amountToFill) {
                        log.info("Buy Order fully filled: {}", headOrder);
                        queue.remove();
                        log.info("Buy Order {} was removed from the OrderBook", headOrder.getId());
                        order.setAmount(0);
                        headOrder.setAmount(0);
                        filled.add(headOrder);
                        bidOrders.replace(price, queue);
                        log.info("Trade {} was fully filled", orderId);
                        remove(order);
                        log.info("Trade {} was removed from the OrderBook", orderId);
                    } else if (availableAmount > amountToFill) {
                        final double remaining = availableAmount - amountToFill;
                        log.info("Buy Order partially filled: {}, Remaining Amount {}", headOrder, remaining);
                        order.setAmount(0);
                        Order orderFilled = headOrder.clone();
                        orderFilled.setAmount(remaining);
                        filled.add(headOrder);
                        update(orderFilled, headOrder);
                        log.info("Trade {} was fully filled", orderId);
                        remove(order);
                        log.info("Trade {} was removed from the OrderBook", orderId);
                    } else {
                        // availableAmount < amountToFill
                        log.info("Buy Order fully filled: {}", headOrder);
                        queue.remove();
                        log.info("Buy Order {} was removed from the OrderBook", headOrder.getId());
                        final double remaining = amountToFill - availableAmount;
                        order.setAmount(remaining);
                        headOrder.setAmount(0);
                        filled.add(headOrder);
                        bidOrders.replace(price, queue);
                        log.info("Trade {} was partially filled {}, Remaining Amount {}", orderId, remaining);
                    }
                }
            } else {
                log.info("There is not any Buy Order at price $ {} to fill the Order {}", price, orderId);
            }
        }
        return filled;
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
