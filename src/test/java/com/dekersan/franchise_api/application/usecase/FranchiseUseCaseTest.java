package com.dekersan.franchise_api.application.usecase;

import com.dekersan.franchise_api.domain.exception.DuplicateResourceException;
import com.dekersan.franchise_api.domain.exception.ResourceNotFoundException;
import com.dekersan.franchise_api.domain.model.Branch;
import com.dekersan.franchise_api.domain.model.Franchise;
import com.dekersan.franchise_api.domain.model.Product;
import com.dekersan.franchise_api.domain.model.TopStockProduct;
import com.dekersan.franchise_api.domain.port.FranchiseRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseUseCaseTest {

    @Mock
    private FranchiseRepositoryPort repository;

    @InjectMocks
    private FranchiseUseCase useCase;

    private Franchise existingFranchise() {
        return new Franchise("f1", "Burger House", List.of(
                new Branch("b1", "Centro", List.of(new Product("p1", "Hamburguesa", 50)))));
    }

    private void saveReturnsWhatItReceives() {
        when(repository.save(any(Franchise.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    @Test
    void createFranchiseShouldSaveWhenNameIsAvailable() {
        when(repository.existsByName("Burger House")).thenReturn(Mono.just(false));
        saveReturnsWhatItReceives();

        StepVerifier.create(useCase.createFranchise("Burger House"))
                .assertNext(created -> {
                    assertThat(created.id()).isNotBlank();
                    assertThat(created.name()).isEqualTo("Burger House");
                    assertThat(created.branches()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void createFranchiseShouldFailWhenNameAlreadyExists() {
        when(repository.existsByName("Burger House")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.createFranchise("Burger House"))
                .expectError(DuplicateResourceException.class)
                .verify();

        verify(repository, never()).save(any());
    }

    @Test
    void addBranchShouldFailWhenFranchiseDoesNotExist() {
        when(repository.findById("no-existe")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.addBranch("no-existe", "Norte"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void addProductShouldAddItToTheBranch() {
        when(repository.findById("f1")).thenReturn(Mono.just(existingFranchise()));
        saveReturnsWhatItReceives();

        StepVerifier.create(useCase.addProduct("f1", "b1", "Papas", 80))
                .assertNext(updated -> assertThat(updated.branches().getFirst().products())
                        .extracting(Product::name)
                        .containsExactly("Hamburguesa", "Papas"))
                .verifyComplete();
    }

    @Test
    void updateProductStockShouldChangeTheStock() {
        when(repository.findById("f1")).thenReturn(Mono.just(existingFranchise()));
        saveReturnsWhatItReceives();

        StepVerifier.create(useCase.updateProductStock("f1", "b1", "p1", 99))
                .assertNext(updated -> assertThat(updated.branches().getFirst().products().getFirst().stock())
                        .isEqualTo(99))
                .verifyComplete();
    }

    @Test
    void removeProductShouldFailAndNotSaveWhenProductDoesNotExist() {
        when(repository.findById("f1")).thenReturn(Mono.just(existingFranchise()));

        StepVerifier.create(useCase.removeProduct("f1", "b1", "no-existe"))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(repository, never()).save(any());
    }

    @Test
    void getTopStockProductByBranchShouldEmitTheTopProducts() {
        when(repository.findById("f1")).thenReturn(Mono.just(existingFranchise()));

        StepVerifier.create(useCase.getTopStockProductByBranch("f1"))
                .expectNext(new TopStockProduct("b1", "Centro", "p1", "Hamburguesa", 50))
                .verifyComplete();
    }

    @Test
    void updateFranchiseNameShouldFailWhenNameBelongsToAnotherFranchise() {
        when(repository.findById("f1")).thenReturn(Mono.just(existingFranchise()));
        when(repository.existsByNameAndIdNot("Pizza House", "f1")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.updateFranchiseName("f1", "Pizza House"))
                .expectError(DuplicateResourceException.class)
                .verify();
    }
}