Prototype of an Exchange
============
[![java](https://img.shields.io/badge/java-17-brightgreen)](https://github.com/andresort28/quant-test)
[![javanio](https://img.shields.io/badge/java-nio-blue)](https://github.com/andresort28/quant-test)
[![maven](https://img.shields.io/badge/maven-3.8.6-yellowgreen)](https://github.com/andresort28/quant-test)

Simplistic Framework for an Exchange

---
### Description
The goal was created a simplistic framework of an exchange. I use Java NIO in order to have non-blocking I/O sockets and non-blocking data structure.

### Required Software:
* JDK 17
* Maven 3.8.6

### Clone the repository
Clone the repository to any folder in your computer
```sh
git clone https://github.com/andresort28/quant-test.git
```

### Run the Exchange and Script
1. Run the `main()` method of the `Exchange` class.
2. Read the `main()` method of the `Script` class and follow the steps written in javadocs before to run it.

### Run stress-test
- The Script contains the method `populateHugeOrderBook()` in order to create the `400,000` operations in the OrderBook. However, since it takes so long to receive all 400,000 messages in the Exchange, you can test with a smaller case to see that we are guaranteeing `thread-safe` on all operations and still handle an algorithmic complexity of `O(1)` with the data structures used as `ConcurrentHashMap` and `PriorityBlockingQueue` for the OrderBook.

### Implementation Notes
- The idea is to create a simplistic framework, that's why I did not use `Netty` directly as the client-server framework, and I used `NIO` instead.
- Any dependency injection framework is used, so I apply Singleton pattern for the repositories, services and server classes.
- I decided to use `Repository Pattern` instead of `DAO` to be able to scale the framework to use database like Redis with the current Repository layer
- This is a prototype and does not implement an indexing database, so I store duplicate objects (`Order`) in different data structure to guarantee `O(1)` in searching, adding, updating and removing
- Sometimes when the `OrderBook` is printed, the ordering by `createdAt` field is not visually correctly, however the `PriorityBlockingQueue` guarantee `FIFO` with its head.
- Multi-symbol allow (In Production we can use a message broker like Kafka to support multiple orders from multiple symbols instead of SocketChannels)


### For Production 
- We could use a database option like `kdb+` o `Redis` to guarantee fast performance
- We can use `Jedis` (Possibly with JedisPool to be thread-safe), or `Lettuce` or `Redisson` for a better scalability as Redis library for Java
- We could use a message broker like `kafka` to support multiple orders from multiple symbols instead of `SocketChannels` 

### Contact

Copyright (c) 2022 [Andres Ortiz](https://www.linkedin.com/in/andresortiz28).  



