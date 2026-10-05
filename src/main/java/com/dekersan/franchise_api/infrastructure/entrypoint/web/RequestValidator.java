package com.dekersan.franchise_api.infrastructure.entrypoint.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RequestValidator {

    private final Validator validator;

    public <T> Mono<T> validate(T body) {
        var violations = validator.validate(body);
        if (violations.isEmpty()) {
            return Mono.just(body);
        }
        String message = violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.joining("; "));
        return Mono.error(new InvalidRequestException(message));
    }
}