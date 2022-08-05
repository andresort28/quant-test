Prototype of an Exchange
========================
[![java](https://img.shields.io/badge/java-17-brightgreen)](https://github.com/andresort28/quant-test)
[![javanio](https://img.shields.io/badge/java-nio-blue)](https://github.com/andresort28/quant-test)
[![maven](https://img.shields.io/badge/maven-3.8.6-yellowgreen)](https://github.com/andresort28/quant-test)

Simplistic Framework for an Exchange
---

### Description
The goal was created a simplistic framework of an exchange. I used Java NIO in order to have non-blocking I/O sockets.

### Required Software

* JDK 17
* Maven 3.8.6

### Communication Protocol

The message is encoded in a similar way to how Financial Information eXchange (FIX) protocol works.

Tag | Description | Values | Description
--- | --- | --- | ---
0 | BeginString | *BITSO* | Constant value
1 | MessageType | *A*,*D*,*M*, *P* | Add, Delete, Modify, Print
2 | OrderSide | *B*,*S* | Buy, Sell
3 | Price | e.g. 100.0 | Order Price
4 | Amount | e.g. 72 | Order Volume
5 | OrderId | e.g. 123e4567-e89b-12d3-a456-426614174000 | Unique UUID
6 | Market | e.g. *BTC_USD* | Symbol Market

Read more about FIX protocol: [here](https://www.fixtrading.org/what-is-fix/)

### Messages examples

ADD Message
```sh
0=BITSO;1=A;2=B;3=23728.9;4=0.01;6=BTC_USD
```

DELETE Message
```sh
0=BITSO;1=D;5=12300000-0000-0000-0000-000000000000
```

MODIFY Message
```sh
0=BITSO;1=M;4=0.02;5=12300000-0000-0000-0000-000000000000
```

PRINT Message
```sh
0=BITSO;1=P;6=BTC_USD
```

### Clone the repository
Clone the repository to any folder in your computer
```sh
git clone https://github.com/andresort28/quant-test.git
```

### Run a complete test
Run the following steps to take a complete test in order to populate an OrderBook and fully filled a trade:

1. Start the `Exchange` server just running the `main()` method of the Exchange class.
2. Uncomment only `populateOrderBook()` in the `Script.main()` method and run. It will populate `BTC/USD` Market with BUY and SELL Orders.
3. View the console logs in the `Exchange` terminal and select an Order in the OrderBook you want to fill. Take a look that the method `Script.executeTrade()` will send a BUY Order (by default), so you can choose an Order to fill in the `Ask Side` of the OrderBook and then, you can edit the variables `price` and `amount` inside `Script.executeTrade()` method in order to fill those SELL Orders you want partially or completely.
4. Comment `populateOrderBook()` and uncomment only `executeTrade()` and `printOrderBook()` in `Script.main()` method and run. It will try to fill the new BUY Trade you send to the Exchange against the Order you choose above. It will also print the final OrderBook state.
5. View the console logs in the `Exchange` terminal to see the entire process.

### Time complexity
The solution is guaranteeing `thread-safe` on all operations and still handle a time complexity of `O(1)` and `O(logn)` for the most operations with `ConcurrentHashMap` and `PriorityBlockingQueue` as non-blocking data structure for the OrderBooks.

- `O(1)` at time to SEARCH an Order in the Orders Maps.
  - `Map` used to store Orders by its OrderId (UUID) as key.
- `O(1)` at time to SEARCH the OrderBook where an Order is. 
  - `Map` used to store OrderBooks by Markets as key. 
- `O(1)` at time to SEARCH the Orders in its respective OrderBook. 
  - `Map` is used to store Order's Queues by Prices as key.
- `O(1)` at time to SEARCH the highest priority Order in an OrderSide (Ask/Bid)
  - `Queue` is used to store Orders using the creation date as the defined priority. 
- `O(logn)` at time to ADD a new Order in its respective OrderSide (Ask/Bid) in the OrderBook. 
  - `Queue` is used to store the Orders in (First-In First) FIFO order, but in this case it is a PriorityBlockingQueue.
- `O(logn)` (worst case) at time to DELETE an Order that is fully filled in the OrderBook. 
  - Each OrderSide uses a `Queue` so the `.remove()` method, remove the head of the Queue. 
  - However, when an arbitrary Order needs to be removed from the OrderBook (Queue) it could be `O(n)` in the worst case to find its index and remove it. 
  - It's just `0.025 ms` to search for it among 2000 Orders with a `CPU=IntelCorei9` and `RAM=16GB`).
- `O(logn)` (worst case) at time to MODIFY an Order that is partially filled in the OrderBook where `n` is the total of Orders at the same Price. 
  - Because OrderBook uses Queues, the Order must be first removed from the Queue arbitrarily, which implies the same time complexity of DELETE operation, and then it needs to be added again.

Note: Because this solution is just a `prototype`, it does not use any database neither any kind of indexing. However, if we used indexing it could reduce the time complexity of DELETE operation to `O(1)`.

### Stress-tests

The `Script` contains the two following stress-tests:

- Small stress-test:
  - The method `populateSmallOrderBook()` create `5,000` Orders in the OrderBook (500 orders for each of the 5 prices on
    each side).
  - It is only the 1,25% of the Huge stress-test, but it's faster to run to measure processing time.
  - The following times we captured with this stress-test on a CPU Intel Core i9 and a RAM of 16GB:
    - ADD Messages
      - Message | Amount | Orders filled | Time
        --- | --- | --- | ---
        ADD | 10 | 1 | 0.000775s
        ADD | 100 | 2 | 0.000951s
        ADD | 500 | 15 | 0.001231s
        ADD | 1000 | 21 | 0.001006s
    - MODIFY Messages (by UUID)
      - Message | Amount | Bigger Amount | Time
        --- | --- | --- | ---
        MODIFY | 39 | 40 | 0.0018s
        MODIFY | 86 | 90 | 0.000753s
        MODIFY | 27 | 30 | 0.000904s
        MODIFY | 2 | 99 | 0.000611s
    - MODIFY Messages (by UUID)
      - Message | Amount | Smaller Amount | Time
        --- | --- | --- | ---
        MODIFY | 90 | 10 | 0.000831s
        MODIFY | 3 | 1 | 0.000902s
        MODIFY | 68 | 30 | 0.000743s
        MODIFY | 71 | 2 | 0.000850S
    - DELETE Messages (by UUID)
      - Message | Time
        --- | ---
        DELETE | 0.000922s
        DELETE | 0.000559s
        DELETE | 0.000718s
        DELETE | 0.000239s

- Huge stress-test:
  - The method `populateHugeOrderBook()` create `400,000` Orders in the OrderBook (2000 orders for each of the 100
    prices on each side).
  - However, since it takes so long to receive all 400,000 messages in the Exchange, you can stress the Exchange with
    the `Small stress-test` to verify the above results and make sure that it guarantee `thread-safe` on all operations
    and a time complexity of `O(1)` and `O(logn)` in the all the operation in the OrderBook.

### Implementation Notes
- The idea is to create a simplistic framework, that's why I did not use `Netty` directly as the client-server framework, and I used `NIO` instead.
- Any dependency injection framework is used, so I apply Singleton pattern for the repositories, services and server classes.
- I decided to use `Repository Pattern` instead of `DAO` to be able to scale the framework to use database like Redis with the current Repository layer
- This is a prototype and does not implement an indexing database, so I stored duplicate objects `Order` (In Orders Maps and OrderBook Maps) to guarantee `O(1)` in search, add, update and delete operations.
- On rare occasions, the `OrderBook` is printed without showing all the items ordered by the `createdAt` field. However, this is not a problem per se, rather the `PriorityBlockingQueue` mainly guarantees `FIFO` with the element in its head.

### For Production 
- We could use a database option like `kdb+` o `Redis` to guarantee fast performance
- We can use `Jedis` (Possibly with JedisPool to be thread-safe), or `Lettuce` or `Redisson` for a better scalability as Redis library for Java
- We could use a message broker like `kafka` to support multiple orders from multiple symbols instead of `SocketChannels` 

### Screenshots

1. Run the Exchange

![image](https://user-images.githubusercontent.com/10570609/182946588-d88d2e2d-6cac-4cbb-80a5-2256313a4c29.png)

2. Run the Script to send messages to the Exchange to populate an OrderBook (BTC_USD)

![image](https://user-images.githubusercontent.com/10570609/182946880-30079f53-6aff-4aaf-a047-ebf7238e8680.png)

3. Exchange receives the message and process each connection printing the Orders and OrderBook Maps

![image](https://user-images.githubusercontent.com/10570609/182947902-78a49c84-bf25-4631-ae96-3d2759fc0b94.png)

![image](https://user-images.githubusercontent.com/10570609/182948049-c0a37a6b-3695-4dde-887c-c257685d8acf.png)

4. Run the Script to send a new Buy Trade (price=400, amount=10) to the Exchange (To fill 2 Sell Orders at price=400)

![image](https://user-images.githubusercontent.com/10570609/182948885-aa14c95e-d2ce-481c-80ba-01a84964ff76.png)

5. Exchange add the new Buy Order and the Matching Engine try to fill the new Trade with the 1st and 2nd Sell Orders (price=400) in the Ask Side with FIFO order

![image](https://user-images.githubusercontent.com/10570609/182949471-55a754a0-8e61-4e5f-81c6-6b15062d6096.png)

6. Run the Script to send a new Print message to the Exchange

![image](https://user-images.githubusercontent.com/10570609/182949108-eb252a14-d6c6-44b5-920f-bf71e544e5a9.png)

7. Exchange prints the final OrderBook with the 1st Sell Order (price=400) with new amount of 50 (before 51)

![image](https://user-images.githubusercontent.com/10570609/182949578-9e8b1457-981d-4607-8ca6-64e827b91d1b.png)

---

### Contact

Copyright (c) 2022 [Andres Ortiz](https://www.linkedin.com/in/andresortiz28).  
