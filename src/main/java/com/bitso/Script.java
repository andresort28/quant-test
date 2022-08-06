package com.bitso;

import com.bitso.model.Market;
import com.bitso.model.Message;
import com.bitso.model.MessageType;
import com.bitso.model.OrderSide;
import com.bitso.shared.Encoder;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

public class Script {

    private static final long MS = 100;

    /**
     * Run the following steps to take a complete test: Populate an OrderBook and fully filled a trade:
     * <ul>
     *     <li>1. Start the Exchange server just running the {@code main()} method in the {@link Exchange} class.</li>
     *     <li>2. Uncomment only {@link #populateOrderBook()} in this {@code main()} method and run. It will populate
     *     BTC_USD Market with BUY and SELL Orders.</li>
     *     <li>3. View the console logs in the {@code Exchange} terminal and select an Order in the OrderBook you want to
     *     filled. The method {@link #executeTrade()} will send a BUY Order, so you can choose an Order to fill in the {@code Ask Side}
     *     of the OrderBook and then, you can edit the variables {@code price} and {@code amount} in {@link #executeTrade()}
     *     method in order to fill that SELL Order you choose partially or completely.</li>
     *     <li>4. Comment {@link #populateOrderBook()} and Uncomment only {@link #executeTrade()} and {@link #print()}
     *     in this {@code main()} method and run. It will try to fill the new Order you send to the Exchange against the
     *     order you decided to select visually. It will also print the final OrderBook state</li>
     *     <li>5. View the console logs in the {@code Exchange} terminal to see entire process.</li>
     *     <li>6. BONUS TRACK: To calculate the Equilibrium mid-market price (EP) given a sample case, only uncomment
     *     {@link #testEquilibriumMidMarketPrice()} in the {@code main} method and run it.</li>
     * </ul>
     *
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        //--Populate OrderBook for BTC_USD without any possible matching operation
        //populateOrderBook();

        //--Test Matching Engine creating an Order in BTC_USD that match a price in the populated OrderBook
        //executeTrade();

        //--Print the Orders and the OrderBook currently on the Exchange for Market BTC_USD
        //print();

        //--Test Individual messages
        //createOrder();
        //removeOrder();
        //updateOrder();

        //--Small stress-test (Read the method javadocs first)
        //populateSmallOrderBook();

        //--Huge stress-test (Read the method javadocs first)
        //populateHugeOrderBook();

        //--BONUS TRACK: function to calculate the equilibrium mid-market price (EP)
        //testEquilibriumMidMarketPrice();
    }

    /**
     * Populate a new simple OrderBook
     * <p>
     * 3 price levels at both sides with 4 orders at each level.
     * <p>
     * It is equivalent to 12 Orders per Side (Ask & Bid). A total of 24 Orders in the OrderBook.
     */
    private static void populateOrderBook() {
        final int initialPrice = 100;
        final int minPriceUnit = 100;
        final int priceLevels = 3;
        final int ordersPerLevel = 4;

        //-- Buy Orders     from $100 to $300       4 Orders per Price level
        //-- Sell Orders    from $400 to $600       4 Orders per Price level
        populateOrderBook(initialPrice, priceLevels, minPriceUnit, ordersPerLevel);
    }

    /**
     * Send a new Order (Trade) to the Exchange
     */
    private static void executeTrade() {
        Message msg = Message.builder()
                .messageType(MessageType.ADD)
                .orderSide(OrderSide.BUY)
                .price(400)
                .amount(5)
                .market(Market.BTC_USD)
                .build();
        sendMessage(msg);
    }

    /**
     * Print the Orders and the current state of the BTC/USD OrderBook
     */
    private static void print() {
        Message msg = Message.builder()
                .messageType(MessageType.PRINT)
                .market(Market.BTC_USD)
                .build();
        sendMessage(msg);
    }

    /**
     * Send a new Order to the Exchange
     */
    private static void createOrder() {
        Message msg = Message.builder()
                .messageType(MessageType.ADD)
                .orderSide(OrderSide.BUY)
                .price(300)
                .amount(50)
                .market(Market.BTC_USD)
                .build();
        sendMessage(msg);
    }

    /**
     * Delete an existing Order in the Exchange
     */
    private static void removeOrder() {
        Message msg = Message.builder()
                .messageType(MessageType.DELETE)
                .orderId(UUID.fromString("91f499dc-e70d-44c3-b776-aecb48b0bfa0"))
                .build();
        sendMessage(msg);
    }

    /**
     * Modify an existing Order in the Exchange
     */
    private static void updateOrder() {
        Message msg = Message.builder()
                .messageType(MessageType.MODIFY)
                .orderId(UUID.fromString("c9bf81f5-02d8-4ebe-8316-9f6cc0c1352c"))
                .amount(99)
                .build();
        sendMessage(msg);
    }

    /**
     * Populate a small OrderBook for a stress-test.
     * <p>
     * 1,25% of the {@link #populateHugeOrderBook()} test.
     * <p>
     * 5 price levels at both sides with 500 orders at each level.
     * It is equivalent to 2,500 Orders per Side (Ask & Bid). A total of 5,000 Orders in the OrderBook.
     * <p>
     * For a better processing time, change the Logger configuration for INFO level only, in the file
     * {@code /resources/logback.xml} and run the Exchange server again.
     */
    private static void populateSmallOrderBook() {
        final int initialPrice = 100;
        final int minPriceUnit = 1;
        final int priceLevels = 5;
        final int ordersPerLevel = 500;

        //-- Buy Orders     from $100 to $104       500 Orders per Price level
        //-- Sell Orders    from $105 to $109       500 Orders per Price level
        populateOrderBook(initialPrice, priceLevels, minPriceUnit, ordersPerLevel);
    }

    /**
     * Populate a Huge OrderBook for a stress-test.
     * <p>
     * 100 price levels at both sides with 2000 orders at each level.
     * It is equivalent to 200,000 Orders per Side (Ask & Bid). A total of 400,000 Orders in the OrderBook.
     * <p>
     * For a better processing time, change the Logger configuration for INFO level only, in the file
     * {@code /resources/logback.xml} and run the Exchange server again.
     */
    private static void populateHugeOrderBook() {
        final int initialPrice = 100;
        final int minPriceUnit = 1;
        final int priceLevels = 100;
        final int ordersPerLevel = 2000;

        //-- Buy Orders     from $100 to $199       2000 Orders per Price level
        //-- Sell Orders    from $200 to $299       2000 Orders per Price level
        populateOrderBook(initialPrice, priceLevels, minPriceUnit, ordersPerLevel);
    }

    /**
     * Populate OrderBook given the lowest price, the level per price, the minimum price unit and the total of
     * orders creating per price level.
     *
     * @param initialPrice
     * @param priceLevels
     * @param minPriceUnit
     * @param ordersPerLevel
     */
    private static void populateOrderBook(int initialPrice, int priceLevels, int minPriceUnit, int ordersPerLevel) {
        RandomGenerator gen = RandomGenerator.of("L128X256MixRandom");

        // Add Buy Orders
        for (int i = initialPrice; i < initialPrice + (priceLevels * minPriceUnit); i += minPriceUnit) {
            for (int j = 0; j < ordersPerLevel; j++) {
                Message msg = Message.builder()
                        .messageType(MessageType.ADD)
                        .orderSide(OrderSide.BUY)
                        .price(i)
                        .amount(gen.nextInt(100))
                        .market(Market.BTC_USD)
                        .build();
                sendMessage(msg);
            }
        }
        System.out.println("--SPREAD");
        // Add Sell Orders
        for (int i = initialPrice + (priceLevels * minPriceUnit); i < initialPrice + (priceLevels * minPriceUnit) * 2; i += minPriceUnit) {
            for (int j = 0; j < ordersPerLevel; j++) {
                Message msg = Message.builder()
                        .messageType(MessageType.ADD)
                        .orderSide(OrderSide.SELL)
                        .price(i)
                        .amount(gen.nextInt(100))
                        .market(Market.BTC_USD)
                        .build();
                sendMessage(msg);
            }
        }
    }

    /**
     * Calculate Equilibrium Mid-Market Price with the sample case given a {@code halfLife} parameter of 0.5 hardcoded in {@link Exchange#process(String)}
     * <p>
     * This prototype use the {@link MessageType#PRINT} message to trigger the calculations, because it's not part of the communication protocol yet.
     */
    private static void testEquilibriumMidMarketPrice() {
        //Populate OrderBook for a sample test of equilibrium mid-market price
        createOrderAndSend(OrderSide.BUY, 7, 10000);
        createOrderAndSend(OrderSide.BUY, 8, 3000);
        createOrderAndSend(OrderSide.BUY, 9, 4500);
        createOrderAndSend(OrderSide.SELL, 10, 1000);
        createOrderAndSend(OrderSide.SELL, 11, 10000);
        createOrderAndSend(OrderSide.SELL, 12, 2500);

        //EP = ~ $9.671
        print();
    }

    private static void createOrderAndSend(OrderSide side, double price, double amount) {
        Message msg = Message.builder()
                .messageType(MessageType.ADD)
                .orderSide(side)
                .price(price)
                .amount(amount)
                .market(Market.BTC_USD)
                .build();
        sendMessage(msg);
    }

    /**
     * Encode the message according to the communication protocol and send it to the Exchange
     *
     * @param msg
     */
    private static void sendMessage(Message msg) {
        String message = Encoder.encode(msg);
        System.out.println("Message sent: " + message);
        try {
            Client client = new Client();
            client.sendMessage(message);
            client.stop();
            TimeUnit.MILLISECONDS.sleep(MS);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
