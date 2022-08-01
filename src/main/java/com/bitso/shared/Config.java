package com.bitso.shared;

import java.net.InetSocketAddress;

public final class Config {

    public static final int PORT = 8081;
    public static final InetSocketAddress BIND_ADDRESS = new InetSocketAddress("localhost", PORT);
    public static final int BUFFER_CAPACITY = 57;

    private Config() {
    }
}
