package com.bitso.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * Data structure to be used for communication between Exchange and actors of the market.
 * Similar to the current Financial Information eXchange (FIX) protocol. Use of tags for each field
 * @author Andres Ortiz
 */
@Builder
@Getter
@Setter
@ToString
public class Message {

    private MessageType messageType;
    private OrderSide orderSide;
    private Double price;
    private Double amount;
    private UUID orderId;
}