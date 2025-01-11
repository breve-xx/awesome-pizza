package org.altervista.breve.awesome.pizza.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "An empty order can't be submitted")
public class EmptyOrderException extends IllegalArgumentException {
}
