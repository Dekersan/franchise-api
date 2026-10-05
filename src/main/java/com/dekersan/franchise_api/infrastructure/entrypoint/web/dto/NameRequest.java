package com.dekersan.franchise_api.infrastructure.entrypoint.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record NameRequest(
        @Schema(description = "Nombre", example = "Burger House")
        @NotBlank(message = "El nombre es obligatorio")
        String name
) {
}