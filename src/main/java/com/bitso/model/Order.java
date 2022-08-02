package com.bitso.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Class representing an Order in the exchange
 *
 * @author Andres Ortiz
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Order {

    @NonNull
    private UUID id;

    @NonNull
    private Market market;

    @NonNull
    private OrderSide side;

    @NonNull
    private double price;

    @Setter
    @NonNull
    private double amount;

    private final Instant createdAt = Instant.now();

    @Setter
    private Instant modifiedAt = createdAt;
}
