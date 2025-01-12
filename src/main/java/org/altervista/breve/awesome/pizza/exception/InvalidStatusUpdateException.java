package org.altervista.breve.awesome.pizza.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY, reason = "The status cannot be set")
public class InvalidStatusUpdateException extends RuntimeException {
}
