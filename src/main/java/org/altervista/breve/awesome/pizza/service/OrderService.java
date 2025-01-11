package org.altervista.breve.awesome.pizza.service;

import org.altervista.breve.awesome.pizza.model.Order;
import org.altervista.breve.awesome.pizza.model.request.SubmitOrderRequest;
import org.altervista.breve.awesome.pizza.utils.UUIDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final UUIDUtils utils;

    @Autowired
    public OrderService(UUIDUtils utils) {
        this.utils = utils;
    }

    public UUID submit(final SubmitOrderRequest request) {
        return utils.get();
    }


    public List<Order> allOrders() {
        return Collections.emptyList();
    }

    public Optional<Order> getOrder(final String name) {
        try {
            final UUID uuid = UUID.fromString(name);
            return Optional.empty();
        } catch (final IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
