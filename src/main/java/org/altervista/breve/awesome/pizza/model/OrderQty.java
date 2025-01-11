package org.altervista.breve.awesome.pizza.model;

import org.altervista.breve.awesome.pizza.exception.InvalidOrderQtyException;

public record OrderQty(int qty) {

    public OrderQty(int qty) {
        if (qty <= 0) {
            throw new InvalidOrderQtyException();
        }
        this.qty = qty;
    }
}
