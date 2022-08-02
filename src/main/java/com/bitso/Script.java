package com.bitso;

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
    }

    public static void populateOrderBook() {
        RandomGenerator gen = RandomGenerator.of("L128X256MixRandom");

        //Add Sell Orders to BTC_USD Market
        for (int i = 4; i < 7; i++) {
            for (int j = 1; j < 5; j++) {
                String message = "0=BITSO;1=A;2=S;3=" + (i * 100) + ";4=" + gen.nextInt(100) + ";6=BTC_USD";
                sendMessage(message);
            }
        }

        //Add Buy Orders to BTC_USD Market
        for (int i = 1; i < 4; i++) {
            for (int j = 1; j < 5; j++) {
                String message = "0=BITSO;1=A;2=B;3=" + (i * 100) + ";4=" + gen.nextInt(100) + ";6=BTC_USD";
                sendMessage(message);
            }
        }
    }

    public static void executeTrade() {
        final String price = "400";
        final String amount = "5";
        String message = "0=BITSO;1=A;2=B;3=" + price + ";4=" + amount + ";6=BTC_USD";
        sendMessage(message);
    }

    public static void printOrderBook() {
        String message = "P=BTC_USD";
        sendMessage(message);
    }

    public static void createOrder() {
        String message = "0=BITSO;1=A;2=B;3=300;4=50;6=BTC_USD";
        sendMessage(message);
    }

    public static void removeOrder() {
        String uuid = "91f499dc-e70d-44c3-b776-aecb48b0bfa0";
        removeOrder(uuid);
    }

    public static void removeOrder(String uuid) {
        String message = "0=BITSO;1=D;5=" + uuid;
        sendMessage(message);
    }

    public static void updateOrder() {
        String uuid = "c9bf81f5-02d8-4ebe-8316-9f6cc0c1352c";
        String amount = "99";
        updateOrder(uuid, amount);
    }

    public static void updateOrder(String uuid, String amount) {
        String message = "0=BITSO;1=M;4=" + amount + ";5=" + uuid;
        sendMessage(message);
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
