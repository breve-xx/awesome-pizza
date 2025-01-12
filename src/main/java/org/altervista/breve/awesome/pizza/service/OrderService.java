package org.altervista.breve.awesome.pizza.service;

import org.altervista.breve.awesome.pizza.exception.EmptyOrderException;
import org.altervista.breve.awesome.pizza.exception.InvalidOrderCodeException;
import org.altervista.breve.awesome.pizza.exception.InvalidStatusUpdateException;
import org.altervista.breve.awesome.pizza.model.Order;
import org.altervista.breve.awesome.pizza.model.OrderPizza;
import org.altervista.breve.awesome.pizza.model.OrderQty;
import org.altervista.breve.awesome.pizza.model.OrderStatus;
import org.altervista.breve.awesome.pizza.model.Pizza;
import org.altervista.breve.awesome.pizza.model.request.SubmitOrderRequest;
import org.altervista.breve.awesome.pizza.repository.OrderRepository;
import org.altervista.breve.awesome.pizza.utils.UUIDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class OrderService {

    private final UUIDUtils utils;
    private final OrderRepository repository;

    @Autowired
    public OrderService(UUIDUtils utils, OrderRepository repository) {
        this.utils = utils;
        this.repository = repository;
    }

    public UUID submit(final SubmitOrderRequest request) {
        if (request == null || request.order() == null || request.order().isEmpty()) {
            throw new EmptyOrderException();
        }

        final Map<Pizza, Integer> pizzas = new HashMap<>();

        request.order().forEach(e -> {
            final Pizza pizza = new OrderPizza(e.name().toUpperCase()).pizza();
            final int qty = new OrderQty(e.qty()).qty();
            pizzas.computeIfPresent(pizza, (k, v) -> v + qty);
            pizzas.putIfAbsent(pizza, qty);
        });

        return repository.save(new Order(utils.get(), OrderStatus.READY, pizzas)).id();
    }


    public List<Order> findNotCompletedOrders() {
        return Stream.concat(
                repository.findByStatus(OrderStatus.IN_PROGRESS).stream(),
                repository.findByStatus(OrderStatus.READY).stream()
        ).toList();
    }

    public Optional<Order> getOrder(final String orderCode) {
        try {
            return repository.findById(UUID.fromString(orderCode));
        } catch (final IllegalArgumentException e) {
            throw new InvalidOrderCodeException();
        }
    }

    public void updateStatus(final Order order, final OrderStatus status) {
        if (order.status() != status) {
            switch (status) {
                case READY -> throw new InvalidStatusUpdateException();
                case IN_PROGRESS -> {
                    if (order.status() == OrderStatus.READY && repository.findByStatus(OrderStatus.IN_PROGRESS).isEmpty()) {
                        repository.save(new Order(order.id(), OrderStatus.IN_PROGRESS, order.pizzas()));
                        break;
                    }
                    throw new InvalidStatusUpdateException();
                }
                case DELIVERED -> {
                    if (order.status() == OrderStatus.IN_PROGRESS) {
                        repository.save(new Order(order.id(), OrderStatus.DELIVERED, order.pizzas()));
                        break;
                    }
                    throw new InvalidStatusUpdateException();
                }
            }
        }
    }
}
