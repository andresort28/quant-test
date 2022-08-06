package com.bitso.services;

import com.bitso.exception.MessageNotSupportedException;
import com.bitso.exception.OrderNotFoundException;
import com.bitso.model.Message;
import com.bitso.model.MessageType;
import com.bitso.model.Order;
import com.bitso.repository.OrderBookRepository;
import com.bitso.repository.OrderBookRepositoryImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Implementation of {@link OrderService}
 *
 * @author Andres Ortiz
 */
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static OrderServiceImpl INSTANCE;
    private final OrderBookRepository orderBookRepository = OrderBookRepositoryImpl.getInstance();

    /**
     * Get Singleton instance
     *
     * @return
     */
    public static OrderService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OrderServiceImpl();
        }
        return INSTANCE;
    }

    @Override
    public Order parseOrder(Message msg) throws MessageNotSupportedException {
        if (msg.getMessageType() != MessageType.ADD) {
            throw new MessageNotSupportedException("The Message to parse is not of type MessageType.ADD");
        }
        return new Order(UUID.randomUUID(), msg.getMarket(), msg.getOrderSide(), msg.getPrice(), msg.getAmount());
    }

    @Override
    public void addOrder(Order order) {
        log.info("Order to add: {}", order);
        orderBookRepository.add(order);
    }

    @Override
    public void deleteOrder(UUID orderId) throws OrderNotFoundException {
        log.info("Order to delete: {}", orderId);
        Order order = orderBookRepository.get(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order " + orderId + " not found to be deleted");
        }
        orderBookRepository.remove(order);
    }

    @Override
    public void modifyOrder(UUID orderId, double newAmount) throws OrderNotFoundException {
        Order order = orderBookRepository.get(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order " + orderId + " not found to be updated");
        }
        log.info("Order to modify: {}, Amount: {}, New Amount: {}", orderId, order.getAmount(), newAmount);
        Order cOrder = order.clone();
        if (newAmount != cOrder.getAmount()) {
            cOrder.setAmount(newAmount);
            orderBookRepository.update(cOrder);
        } else {
            log.info("Order " + orderId + " was not modified because the new amount is the same");
        }
    }

    @Override
    public void printOrders() {
        log.debug("--Orders Map");
        orderBookRepository.getOrders().forEach(order -> log.debug("----{}", order));
    }

    private OrderServiceImpl() {
    }
}
