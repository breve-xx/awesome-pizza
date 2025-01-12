package org.altervista.breve.awesome.pizza.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateTimeUtils {

    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
