package com.bitso;

import com.bitso.model.Market;
import com.bitso.repository.OrderBookRepositoryImpl;

import java.io.IOException;
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
     *     <li>4. Comment {@link #populateOrderBook()} and Uncomment only {@link #executeTrade()} and {@link #printOrderBook()}
     *     in this {@code main()} method and run. It will try to fill the new Order you send to the Exchange against the
     *     order you decided to select visually. It will also print the final OrderBook state</li>
     *     <li>5. View the console logs in the {@code Exchange} terminal to see entire process.</li>
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

        //--Print the current OrderBook in the Exchange for Market BTC_USD
        //printOrderBook();

        //--Test Individual messages
        //createOrder();
        //removeOrder();
        //updateOrder();

        //--For stress-test purposes only. Read the javadocs before to run it
        //populateHugeOrderBook();
    }

    private static void populateOrderBook() {
        RandomGenerator gen = RandomGenerator.of("L128X256MixRandom");
        //Add Buy Orders to BTC_USD Market
        for (int i = 1; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                String message = "0=BITSO;1=A;2=B;3=" + (i * 100) + ";4=" + gen.nextInt(100) + ";6=BTC_USD";
                sendMessage(message);
            }
        }
        //Add Sell Orders to BTC_USD Market
        for (int i = 4; i < 7; i++) {
            for (int j = 0; j < 4; j++) {
                String message = "0=BITSO;1=A;2=S;3=" + (i * 100) + ";4=" + gen.nextInt(100) + ";6=BTC_USD";
                sendMessage(message);
            }
        }
    }

    private static void executeTrade() {
        final String price = "400";
        final String amount = "5";
        String message = "0=BITSO;1=A;2=B;3=" + price + ";4=" + amount + ";6=BTC_USD";
        sendMessage(message);
    }

    private static void printOrderBook() {
        String message = "P=BTC_USD";
        sendMessage(message);
    }

    private static void createOrder() {
        String message = "0=BITSO;1=A;2=B;3=300;4=50;6=BTC_USD";
        sendMessage(message);
    }

    private static void removeOrder() {
        String uuid = "91f499dc-e70d-44c3-b776-aecb48b0bfa0";
        removeOrder(uuid);
    }

    private static void removeOrder(String uuid) {
        String message = "0=BITSO;1=D;5=" + uuid;
        sendMessage(message);
    }

    private static void updateOrder() {
        String uuid = "c9bf81f5-02d8-4ebe-8316-9f6cc0c1352c";
        String amount = "99";
        updateOrder(uuid, amount);
    }

    private static void updateOrder(String uuid, String amount) {
        String message = "0=BITSO;1=M;4=" + amount + ";5=" + uuid;
        sendMessage(message);
    }

    /**
     * Populate a Huge OrderBook for a stress-test. 100 price level at both sides with 2000 orders at each level.
     * <p>
     * It is equivalent to 200,000 Orders per Side (Ask & Bid). A total of 400,000 Orders in the OrderBook.
     * <p>
     * For this test, comment the body of the methods {@link OrderBookRepositoryImpl#printOrders()} and
     * {@link OrderBookRepositoryImpl#printOrderBook(Market)} ()} to skip printing all the Orders and the OrderBook
     * each time the Exchange received a new message.
     */
    private static void populateHugeOrderBook() {
        RandomGenerator gen = RandomGenerator.of("L128X256MixRandom");

        final int finalPriceSell = 300;
        final int initialPriceSell = 201;
        // Market Spread
        final int finalPriceBuy = 200;
        final int initialPriceBuy = 100;

        final int ordersPerLevel = 2000;

        //Add Buy Orders to BTC_USD Market
        for (int i = initialPriceBuy; i <= finalPriceBuy; i++) {
            for (int j = 0; j < ordersPerLevel; j++) {
                String message = "0=BITSO;1=A;2=B;3=" + i + ";4=" + gen.nextInt(100) + ";6=BTC_USD";
                sendMessage(message);
            }
        }
        //Add Sell Orders to BTC_USD Market
        for (int i = initialPriceSell; i <= finalPriceSell; i++) {
            for (int j = 0; j < ordersPerLevel; j++) {
                String message = "0=BITSO;1=A;2=S;3=" + i + ";4=" + gen.nextInt(100) + ";6=BTC_USD";
                sendMessage(message);
            }
        }
    }

    private static void sendMessage(String message) {
        System.out.println("Message to send: " + message);
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
