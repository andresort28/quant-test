package com.bitso.shared;

import com.bitso.model.Market;
import com.bitso.model.Message;

import java.util.Optional;
import java.util.UUID;

/**
 * Encoder for a message that wants to be sent to the Exchange server-
 * The message is encoded in a similar way to how Financial Information eXchange (FIX) protocol works.
 * <blockquote>
 * <table class="striped">
 * <thead>
 *   <tr>
 *     <th scope="col">Tag</th>
 *     <th scope="col">Description</th>
 *     <th scope="col">Values</th>
 *     <th scope="col">Description</th>
 *   </tr>
 * </thead>
 * <tbody>
 *   <tr>
 *     <th scope="row">0</th>
 *     <td>BeginString</td>
 *     <td>"BITSO"</td>
 *     <td>Constant value</td>
 *   </tr>
 *   <tr>
 *     <th scope="row">1</th>
 *     <td>MessageType</td>
 *     <td>"A","D","M","P"</td>
 *     <td>Add, Delete, Modify, Print</td>
 *   </tr>
 *   <tr>
 *     <th scope="row">2</th>
 *     <td>OrderSide</td>
 *     <td>"B","S"</td>
 *     <td>Buy, Sell</td>
 *   </tr>
 *   <tr>
 *     <th scope="row">3</th>
 *     <td>Price</td>
 *     <td>e.g. 100.0</td>
 *     <td>Order Price</td>
 *   </tr>
 *   <tr>
 *     <th scope="row">4</th>
 *     <td>Amount</td>
 *     <td>e.g. 72.0</td>
 *     <td>Order Volume</td>
 *   </tr>
 *   <tr>
 *     <th scope="row">5</th>
 *     <td>OrderId</td>
 *     <td>e.g. 123e4567-e89b-12d3-a456-426614174000</td>
 *     <td>Unique UUID</td>
 *   </tr>
 *   <tr>
 *     <th scope="row">6</th>
 *     <td>Market</td>
 *     <td>e.g. "BTC_USD"</td>
 *     <td>Market Symbol</td>
 *   </tr>
 * </tbody>
 * </table>
 * </blockquote>
 * <p>
 * Examples:
 * <pre>
 *    "0=BITSO;1=P;6=BTC_USD"                                       -- 21 bytes
 *    "0=BITSO;1=A;2=B;3=23728.9;4=0.01;6=BTC_USD"                  -- 42 bytes
 *    "0=BITSO;1=A;2=S;3=24001.25;4=0.02;6=BTC_USD"                 -- 42 bytes
 *    "0=BITSO;1=D;5=12300000-0000-0000-0000-000000000000"          -- 50 bytes
 *    "0=BITSO;1=M;4=0.02;5=12300000-0000-0000-0000-000000000000"   -- 57 bytes
 * </pre>
 *
 * @author Andres Ortiz
 * @see <a href="https://www.fixtrading.org/what-is-fix/">FIX Protocol</a>
 */
public class Encoder {

    protected static final String DELIMITER = ";";
    protected static final String BEGIN_STRING = "BITSO";

    public static String encode(Message msg) {
        StringBuilder builder = new StringBuilder("0=").append(BEGIN_STRING).append(DELIMITER);
        switch (msg.getMessageType()) {
            case ADD -> builder.append(encodeAddMessage(msg));
            case DELETE -> builder.append(encodeDeleteMessage(msg));
            case MODIFY -> builder.append(encodeModifyMessage(msg));
            case PRINT -> builder.append(encodePrintMessage(msg));
        }
        return builder.toString();
    }

    private static String encodeAddMessage(Message msg) {
        StringBuilder builder = new StringBuilder("1=A").append(DELIMITER)
                .append(encodeSide(msg)).append(DELIMITER)
                .append(encodePrice(msg)).append(DELIMITER)
                .append(encodeAmount(msg)).append(DELIMITER)
                .append(encodeMarket(msg));
        return builder.toString();
    }

    private static String encodeDeleteMessage(Message msg) {
        StringBuilder builder = new StringBuilder("1=D").append(DELIMITER)
                .append(encodeOrderId(msg));
        return builder.toString();
    }

    private static String encodeModifyMessage(Message msg) {
        StringBuilder builder = new StringBuilder("1=M").append(DELIMITER)
                .append(encodeAmount(msg)).append(DELIMITER)
                .append(encodeOrderId(msg));
        return builder.toString();
    }

    private static String encodePrintMessage(Message msg) {
        StringBuilder builder = new StringBuilder("1=P").append(DELIMITER)
                .append(encodeMarket(msg));
        return builder.toString();
    }

    private static String encodeSide(Message msg) {
        return switch (msg.getOrderSide()) {
            case BUY -> "2=B";
            case SELL -> "2=S";
        };
    }

    private static String encodePrice(Message msg) {
        return "3=" + msg.getPrice();
    }

    private static String encodeAmount(Message msg) {
        return "4=" + msg.getAmount();
    }

    private static String encodeOrderId(Message msg) {
        return "5=" + Optional.ofNullable(msg.getOrderId()).orElse(new UUID(0L, 0L));
    }

    private static String encodeMarket(Message msg) {
        return "6=" + Optional.ofNullable(msg.getMarket()).orElse(Market.NONE);
    }
}
