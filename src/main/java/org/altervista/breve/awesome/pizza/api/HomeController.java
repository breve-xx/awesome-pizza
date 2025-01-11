package org.altervista.breve.awesome.pizza.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    private final String apiName;
    private final String apiVersion;

    public HomeController(
            @Value("${awesome.pizza.api.name}") final String apiName,
            @Value("${awesome.pizza.api.version}") final String apiVersion
    ) {
        this.apiName = apiName;
        this.apiVersion = apiVersion;
    }

    @GetMapping
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello from %s %s".formatted(apiName, apiVersion));
    }
}
