package org.altervista.breve.awesome.pizza.service;

import org.altervista.breve.awesome.pizza.dao.OrderDao;
import org.altervista.breve.awesome.pizza.model.*;
import org.altervista.breve.awesome.pizza.model.request.SubmitOrderRequest;
import org.altervista.breve.awesome.pizza.utils.UUIDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OrderService {

    private final UUIDUtils utils;
    private final OrderDao dao;

    @Autowired
    public OrderService(UUIDUtils utils, OrderDao dao) {
        this.utils = utils;
        this.dao = dao;
    }

    public UUID submit(final SubmitOrderRequest request) {
        final Map<Pizza, Integer> pizzas = new HashMap<>();

        request.order().forEach(e -> {
            final Pizza pizza = new OrderPizza(e.name().toUpperCase()).pizza();
            final int qty = new OrderQty(e.qty()).qty();
            pizzas.computeIfPresent(pizza, (k, v) -> v + qty);
            pizzas.putIfAbsent(pizza, qty);
        });

        return dao.save(new Order(utils.get(), OrderStatus.READY, pizzas)).id();
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
