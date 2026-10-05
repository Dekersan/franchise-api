package com.dekersan.franchise_api.infrastructure.entrypoint.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductRequest(
        @Schema(description = "Nombre del producto", example = "Hamburguesa Doble")
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @Schema(description = "Cantidad en stock", example = "50", minimum = "0")
        @NotNull(message = "El stock es obligatorio")
        @PositiveOrZero(message = "El stock no puede ser negativo")
        Integer stock
) {
}