package org.altervista.breve.awesome.pizza.api;

import org.altervista.breve.awesome.pizza.model.Pizza;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pizza")
public class PizzaController {

    @GetMapping
    public ResponseEntity<Pizza[]> list() {
        return ResponseEntity.ok(Pizza.values());
    }
}
