package org.altervista.breve.awesome.pizza.model;

import org.altervista.breve.awesome.pizza.exception.InvalidOrderPizzaException;

public record OrderPizza(Pizza pizza) {

    private static Pizza fromString(String name) {
        try {
            return Pizza.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new InvalidOrderPizzaException();
        }
    }

    public OrderPizza(final String name) {
        this(fromString(name));
    }
}
