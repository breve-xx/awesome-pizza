package org.altervista.breve.awesome.pizza.model.request;

import java.util.List;

public record SubmitOrderRequest(List<OrderEntry> order) {
}
