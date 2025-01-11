package org.altervista.breve.awesome.pizza.utils;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UUIDUtils {

    public UUID get() {
        return UUID.randomUUID();
    }
}
