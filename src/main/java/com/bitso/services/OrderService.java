package com.bitso.services;

import com.bitso.exception.MessageNotSupportedException;
import com.bitso.exception.OrderNotFoundException;
import com.bitso.model.Message;
import com.bitso.model.MessageType;
import com.bitso.model.Order;

import java.util.UUID;

/**
 * Order service to manage Orders operations
 *
 * @author Andres Ortiz
 */
public interface OrderService {

    /**
     * Create a new Order object given a {@link Message} of type {@link MessageType#ADD}
     *
     * @param msg
     * @return new {@link Order} object
     * @throws MessageNotSupportedException when the Message is not an ADD Message
     */
    Order parseOrder(Message msg) throws MessageNotSupportedException;

    /**
     * Add a new Order to the Exchange given an {@link Order} object
     *
     * @param order
     */
    void addOrder(Order order);

    /**
     * Delete an Order from the Exchange given its {@code orderId}
     *
     * @param orderId
     * @throws OrderNotFoundException
     */
    void deleteOrder(UUID orderId) throws OrderNotFoundException;

    /**
     * Modify an existing {@link Order} of the Exchange given its {@code orderId} and the new amount to set.
     *
     * @param orderId
     * @param newAmount
     * @throws OrderNotFoundException
     */
    void modifyOrder(UUID orderId, double newAmount) throws OrderNotFoundException;
}
