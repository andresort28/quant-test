package com.bitso.shared;

import com.bitso.exception.MessageNotSupportedException;
import com.bitso.model.Market;
import com.bitso.model.Message;
import com.bitso.model.MessageType;
import com.bitso.model.OrderSide;

import java.util.UUID;

import static com.bitso.shared.Encoder.BEGIN_STRING;
import static com.bitso.shared.Encoder.DELIMITER;

/**
 * Decoder for a message received by a client.
 * The message is encoded in a similar way to how Financial Information eXchange (FIX) protocol works.
 * Details about encoding method can be found in {@link Encoder}
 *
 * @author Andres Ortiz
 * @see <a href="https://www.fixtrading.org/what-is-fix/">FIX Protocol</a>
 */
public class Decoder {

    private static final int MAX_TAGS = 7;

    public static Message decode(String msg) throws MessageNotSupportedException {
        String[] fields = new String[MAX_TAGS];
        String[] values = msg.split(DELIMITER);
        for (String value : values) {
            String[] pairs = value.split("=");
            int tag = Integer.parseInt(pairs[0]);
            fields[tag] = pairs[1];
        }
        if (!fields[0].equals(BEGIN_STRING)) {
            throw new MessageNotSupportedException("The tag 0 is not a begin string supported by the Exchange");
        }
        MessageType type = getMessageType(fields[1]);
        return switch (type) {
            case ADD -> decodeAddMessage(fields);
            case DELETE -> decodeDeleteMessage(fields);
            case MODIFY -> decodeModifyMessage(fields);
            case PRINT -> decodePrintMessage(fields);
        };
    }

    private static MessageType getMessageType(String value) {
        return switch (value) {
            case "A" -> MessageType.ADD;
            case "D" -> MessageType.DELETE;
            case "M" -> MessageType.MODIFY;
            case "P" -> MessageType.PRINT;
            default -> null;
        };
    }

    private static Message decodeAddMessage(String[] fields) {
        OrderSide orderSide = getOrderSide(fields[2]);
        double price = Double.parseDouble(fields[3]);
        double amount = Double.parseDouble(fields[4]);
        Market market = Market.valueOf(fields[6]);
        return Message.builder()
                .messageType(MessageType.ADD)
                .orderSide(orderSide)
                .price(price)
                .amount(amount)
                .market(market)
                .build();
    }

    private static Message decodeDeleteMessage(String[] fields) {
        UUID orderId = UUID.fromString(fields[5]);
        return Message.builder()
                .messageType(MessageType.DELETE)
                .orderId(orderId)
                .build();
    }

    private static Message decodeModifyMessage(String[] fields) {
        double amount = Double.parseDouble(fields[4]);
        UUID orderId = UUID.fromString(fields[5]);
        return Message.builder()
                .messageType(MessageType.MODIFY)
                .amount(amount)
                .orderId(orderId)
                .build();
    }

    private static Message decodePrintMessage(String[] fields) {
        Market market = Market.valueOf(fields[6]);
        return Message.builder()
                .messageType(MessageType.PRINT)
                .market(market)
                .build();
    }

    private static OrderSide getOrderSide(String value) {
        return switch (value) {
            case "B" -> OrderSide.BUY;
            case "S" -> OrderSide.SELL;
            default -> null;
        };
    }
}
