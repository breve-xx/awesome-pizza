package org.altervista.breve.awesome.pizza.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Invalid quantity in order")
public class InvalidOrderQtyException extends IllegalArgumentException {
}
