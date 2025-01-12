package org.altervista.breve.awesome.pizza.api;

import io.swagger.v3.oas.annotations.Operation;
import org.altervista.breve.awesome.pizza.model.Pizza;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pizza")
public class PizzaController {

    @GetMapping
    @Operation(summary = "All the available Pizzas", description = "These are the names of the Pizza that you can add to your order")
    public ResponseEntity<Pizza[]> list() {
        return ResponseEntity.ok(Pizza.values());
    }
}
