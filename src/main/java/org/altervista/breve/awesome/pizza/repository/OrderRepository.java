package org.altervista.breve.awesome.pizza.repository;

import org.altervista.breve.awesome.pizza.model.Order;
import org.altervista.breve.awesome.pizza.model.OrderStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends MongoRepository<Order, UUID> {

    List<Order> findByStatus(OrderStatus status);
}
