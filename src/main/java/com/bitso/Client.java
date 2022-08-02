package com.bitso;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static com.bitso.shared.Config.BIND_ADDRESS;
import static com.bitso.shared.Config.BUFFER_CAPACITY;


/**
 * Client as a market actor who sends a message to the exchange
 *
 * @author Andres Ortiz
 */
@Slf4j
public class Client {

    private static SocketChannel socketChannel;
    private static ByteBuffer buffer;

    public void stop() throws IOException {
        socketChannel.close();
        buffer = null;
    }

    /**
     *
     */
    public Client() {
        try {
            socketChannel = SocketChannel.open(BIND_ADDRESS);
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
            //--Exchange does not respond to clients (out of the scope of this prototype)--
            //--Read from the Exchange the result of the operation--
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
