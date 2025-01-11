package org.altervista.breve.awesome.pizza.dao;

import org.altervista.breve.awesome.pizza.model.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderDao {

    public Order save(final Order order) {
        return order;
    }
}
