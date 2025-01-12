package org.altervista.breve.awesome.pizza.api;

import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Just say hello!", description = "This API is meant to display the basic API info")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello from %s %s".formatted(apiName, apiVersion));
    }
}
