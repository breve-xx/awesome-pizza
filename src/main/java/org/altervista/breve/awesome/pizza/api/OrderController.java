package org.altervista.breve.awesome.pizza.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
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

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService service;

    @Autowired
    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Submit an order", description = "Add your favourite Pizza to queue and get them delivered as soon as possible")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The order has been submitted and the orderCode for tracking purpose is returned"),
            @ApiResponse(responseCode = "400", description = "You're asking something that we can't or don't want to handle")
    })
    public ResponseEntity<SubmitOrderResponse> submit(@RequestBody SubmitOrderRequest request, final HttpServletRequest httpRequest) {
        final SubmitOrderResponse response = new SubmitOrderResponse(service.submit(request));
        final URI location = URI.create(
                httpRequest.getRequestURL()
                        .append("/")
                        .toString()
        );
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "It's your turn now!", description = "This is the order queue, if there isn't an IN_PROGRESS order just pick one!")
    public ResponseEntity<List<Order>> list() {
        return ResponseEntity.ok(service.findNotCompletedOrders());
    }

    @GetMapping("/{orderCode}")
    @Operation(summary = "Are you hungry?", description = "Use the provided orderCode to keep an eye on your order status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Here's what you ordered and its status, we'll try to deliver it as soon as possible!"),
            @ApiResponse(responseCode = "400", description = "We can't recognize this code, are you sure you ordered from us?!"),
            @ApiResponse(responseCode = "404", description = "Oops, we can't find your order, are you sure the orderCode is correct?!")
    })
    public ResponseEntity<Order> get(@PathVariable final String orderCode) {
        return service.getOrder(orderCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{orderCode}")
    @Operation(summary = "Let's work on it", description = "Pick a READY order or deliver an IN_PROGRESS order, just remember: one at a time!")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "As my father always says: you've done half your duty..."),
            @ApiResponse(responseCode = "400", description = "We don't recognize the code or the status, are you sure you work from us?!"),
            @ApiResponse(responseCode = "404", description = "Oops, we can't find this order, are you sure the orderCode is correct?!"),
            @ApiResponse(responseCode = "422", description = "You shall not pass! [rules: 1. Pick the READY orders one at a time!, 2. Deliver only the IN_PROGRESS order, 3. Never go back to READY]")
    })
    public ResponseEntity<?> update(@PathVariable final String orderCode, @RequestParam final OrderStatus status) {
        return service.getOrder(orderCode)
                .map(order -> {
                    service.updateStatus(order, status);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
