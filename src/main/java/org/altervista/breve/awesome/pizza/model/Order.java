package org.altervista.breve.awesome.pizza.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Document(collection = "orders")
public record Order(
        @Id UUID id,
        LocalDateTime submittedAt,
        OrderStatus status,
        Map<Pizza, Integer> pizzas) {
}