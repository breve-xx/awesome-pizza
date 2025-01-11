package org.altervista.breve.awesome.pizza.model;

import java.util.Map;
import java.util.UUID;

public record Order(UUID id, OrderStatus status, Map<Pizza, Integer> pizzas) {
}