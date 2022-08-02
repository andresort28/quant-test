package com.bitso.model;

import lombok.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Class representing an Order in the exchange
 *
 * @author Andres Ortiz
 */
@Getter
@ToString
@RequiredArgsConstructor
public class Order implements Cloneable{

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

    @Override
    public Order clone() {
        try {
            return (Order) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id.equals(order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
