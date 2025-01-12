package org.altervista.breve.awesome.pizza.api;

import org.altervista.breve.awesome.pizza.model.Order;
import org.altervista.breve.awesome.pizza.model.OrderStatus;
import org.altervista.breve.awesome.pizza.model.request.SubmitOrderRequest;
import org.altervista.breve.awesome.pizza.model.response.SubmitOrderResponse;
import org.altervista.breve.awesome.pizza.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        return ResponseEntity.ok(new SubmitOrderResponse(service.submit(request)));
    }

    @GetMapping
    public ResponseEntity<List<Order>> list() {
        return ResponseEntity.ok(service.findNotCompletedOrders());
    }

    @GetMapping("/{orderCode}")
    public ResponseEntity<Order> get(@PathVariable final String orderCode) {
        return service.getOrder(orderCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{orderCode}")
    public ResponseEntity<?> update(@PathVariable final String orderCode, @RequestParam final OrderStatus status) {
        return service.getOrder(orderCode)
                .map(order -> {
                    service.updateStatus(order, status);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
