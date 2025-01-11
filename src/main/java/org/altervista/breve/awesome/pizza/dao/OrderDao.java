package org.altervista.breve.awesome.pizza.dao;

import org.altervista.breve.awesome.pizza.model.Order;
import org.altervista.breve.awesome.pizza.model.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class OrderDao {

    public Order save(final Order order) {
        return order;
    }

    public List<Order> searchByStatus(final OrderStatus status) {
        return Collections.emptyList();
    }

    public Optional<Order> findByUUID(final UUID uuid) {
        return Optional.empty();
    }
}
