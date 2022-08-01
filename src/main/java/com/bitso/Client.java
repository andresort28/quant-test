package com.bitso;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static com.bitso.Exchange.BUFFER_CAPACITY;
import static com.bitso.Exchange.PORT;

/**
 * Client as a market actor who sends a message to the exchange
 *
 * @author Andres Ortiz
 */
@Slf4j
public class Client {

    private static SocketChannel socketChannel;
    private static ByteBuffer buffer;

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        String message = "0=BITSO;1=D;5=12300000-0000-0000-0000-000000000001";
        client.sendMessage(message);
        client.stop();
    }

    public void stop() throws IOException {
        socketChannel.close();
        buffer = null;
    }

    /**
     *
     */
    public Client() {
        try {
            socketChannel = SocketChannel.open(new InetSocketAddress("localhost", PORT));
            buffer = ByteBuffer.allocate(BUFFER_CAPACITY);
        } catch (IOException e) {
            log.error("Connection to the Exchange was not possible", e);
        }
    }

    /**
     * Send message to the Exchange
     *
     * @param msg
     */
    public void sendMessage(String msg) {
        buffer = ByteBuffer.wrap(msg.getBytes());
        try {
            socketChannel.write(buffer);

            /*
            --Read from the Exchange the result of the operation--
            buffer.clear();
            socketChannel.read(buffer);
            String response = new String(buffer.array()).trim();
            log.info("Response from Exchange: {}", response);
            buffer.clear();
             */
        } catch (IOException e) {
            log.error("Error writing or reading from the Buffer", e);
        }
    }
}
