package com.bitso;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

public class Script {

    private static final long MS = 100;

    public static void main(String[] args) throws IOException, InterruptedException {
        populate();
        //removeOrder();
        //createOrder();
        //updateOrder();
    }

    public static void populate() throws InterruptedException, IOException {
        RandomGenerator gen = RandomGenerator.of("L128X256MixRandom");

        //Add Sell Orders to BTC_USD Market
        for (int i = 4; i < 7; i++) {
            for (int j = 1; j < 5; j++) {
                Client client = new Client();
                String message = "0=BITSO;1=A;2=S;3=" + (i * 100) + ";4=" + gen.nextInt(100) + ";6=BTC_USD";
                System.out.println("Message to send: " + message);
                client.sendMessage(message);
                client.stop();
                TimeUnit.MILLISECONDS.sleep(MS);
            }
        }

        //Add Buy Orders to BTC_USD Market
        for (int i = 1; i < 4; i++) {
            for (int j = 1; j < 5; j++) {
                Client client = new Client();
                String message = "0=BITSO;1=A;2=B;3=" + (i * 100) + ";4=" + gen.nextInt(100) + ";6=BTC_USD";
                System.out.println("Message to send: " + message);
                client.sendMessage(message);
                client.stop();
                TimeUnit.MILLISECONDS.sleep(MS);
            }
        }
    }

    public static void createOrder() throws IOException, InterruptedException {
        Client client = new Client();
        String message = "0=BITSO;1=A;2=B;3=300;4=50;6=BTC_USD";
        System.out.println("Message to send: " + message);
        client.sendMessage(message);
        client.stop();
        TimeUnit.MILLISECONDS.sleep(MS);
    }

    public static void removeOrder() throws IOException {
        String uuid = "91f499dc-e70d-44c3-b776-aecb48b0bfa0";
        removeOrder(uuid);
    }

    public static void removeOrder(String uuid) throws IOException {
        Client client = new Client();
        String message = "0=BITSO;1=D;5=" + uuid;
        System.out.println("Message to send: " + message);
        client.sendMessage(message);
        client.stop();
    }

    public static void updateOrder() throws IOException {
        String uuid = "11a21821-3cd2-4624-a4c9-752cc686617d";
        String amount = "99";
        updateOrder(uuid, amount);
    }

    public static void updateOrder(String uuid, String amount) throws IOException {
        Client client = new Client();
        String message = "0=BITSO;1=M;4=" + amount + ";5=" + uuid;
        System.out.println("Message to send: " + message);
        client.sendMessage(message);
        client.stop();
    }
}
