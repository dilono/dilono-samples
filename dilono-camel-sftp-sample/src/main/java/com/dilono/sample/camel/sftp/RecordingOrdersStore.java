package com.dilono.sample.camel.sftp;

import com.dilono.sample.basic.Order;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Just an in-memory store for orders.
 */
@Component
public class RecordingOrdersStore {

    private final Map<Integer, List<Order>> orders = new ConcurrentHashMap<>();

    @SuppressWarnings("unused")
    public void store(List<Order> orders) {
        this.orders.clear();
        this.orders.put(this.orders.size(), orders);
    }

    int size() {
        return orders.size();
    }

    boolean isEmpty() {
        return orders.isEmpty();
    }

    List<Order> getOrders() {
        return orders.values()
            .stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }
}
