package com.dekersan.franchise_api.infrastructure.entrypoint.web;

import com.dekersan.franchise_api.application.usecase.FranchiseUseCase;
import com.dekersan.franchise_api.domain.exception.DuplicateResourceException;
import com.dekersan.franchise_api.domain.exception.ResourceNotFoundException;
import com.dekersan.franchise_api.domain.model.Franchise;
import com.dekersan.franchise_api.domain.model.TopStockProduct;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FranchiseRouterTest {

    private FranchiseUseCase useCase;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        useCase = mock(FranchiseUseCase.class);
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        var handler = new FranchiseHandler(useCase, new RequestValidator(validator));
        client = WebTestClient.bindToRouterFunction(new FranchiseRouter().franchiseRoutes(handler)).build();
    }

    @Test
    void createFranchiseShouldReturn201() {
        when(useCase.createFranchise("Burger House"))
                .thenReturn(Mono.just(new Franchise("f1", "Burger House", List.of())));

        client.post().uri("/api/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Burger House"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("f1")
                .jsonPath("$.name").isEqualTo("Burger House");
    }

    @Test
    void createFranchiseShouldReturn400WhenNameIsBlank() {
        client.post().uri("/api/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", ""))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("El nombre es obligatorio");
    }

    @Test
    void createFranchiseShouldReturn409WhenNameAlreadyExists() {
        when(useCase.createFranchise("Burger House"))
                .thenReturn(Mono.error(new DuplicateResourceException("Ya existe una franquicia con el nombre 'Burger House'")));

        client.post().uri("/api/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Burger House"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void updateStockShouldReturn400WhenStockIsNegative() {
        client.patch().uri("/api/franchises/f1/branches/b1/products/p1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("stock", -5))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("El stock no puede ser negativo");
    }

    @Test
    void removeProductShouldReturn204() {
        when(useCase.removeProduct("f1", "b1", "p1"))
                .thenReturn(Mono.just(new Franchise("f1", "Burger House", List.of())));

        client.delete().uri("/api/franchises/f1/branches/b1/products/p1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void topStockProductsShouldReturnTheList() {
        when(useCase.getTopStockProductByBranch("f1"))
                .thenReturn(Flux.just(new TopStockProduct("b1", "Centro", "p1", "Papas", 80)));

        client.get().uri("/api/franchises/f1/top-stock-products")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].branchName").isEqualTo("Centro")
                .jsonPath("$[0].productName").isEqualTo("Papas");
    }

    @Test
    void topStockProductsShouldReturn404WhenFranchiseDoesNotExist() {
        when(useCase.getTopStockProductByBranch("no-existe"))
                .thenReturn(Flux.error(new ResourceNotFoundException("Franquicia no encontrada: no-existe")));

        client.get().uri("/api/franchises/no-existe/top-stock-products")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Franquicia no encontrada: no-existe");
    }
}