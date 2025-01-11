package org.altervista.breve.awesome.pizza.api;

import org.altervista.breve.awesome.pizza.model.Order;
import org.altervista.breve.awesome.pizza.model.request.SubmitOrderRequest;
import org.altervista.breve.awesome.pizza.model.response.SubmitOrderResponse;
import org.altervista.breve.awesome.pizza.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderService service;

    @Autowired
    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SubmitOrderResponse> submit(@RequestBody SubmitOrderRequest request) {
        if (request.order() == null || request.order().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An empty order can't be submitted");
        }
        return ResponseEntity.ok(new SubmitOrderResponse(service.submit(request)));
    }

    @GetMapping
    public ResponseEntity<List<Order>> list() {
        return ResponseEntity.ok(service.allOrders());
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<Order> get(@PathVariable final String uuid) {
        return service.getOrder(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
