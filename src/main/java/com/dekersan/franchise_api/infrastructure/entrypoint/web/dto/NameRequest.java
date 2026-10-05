package com.dekersan.franchise_api.infrastructure.entrypoint.web.dto;

import jakarta.validation.constraints.NotBlank;

public record NameRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name
) {
}