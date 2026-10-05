package com.dekersan.franchise_api.domain.model;

import com.dekersan.franchise_api.domain.exception.DuplicateResourceException;
import com.dekersan.franchise_api.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BranchTest {

    private final Product burger = new Product("p1", "Hamburguesa", 50);
    private final Product fries = new Product("p2", "Papas", 80);
    private final Branch branch = new Branch("b1", "Centro", List.of(burger, fries));

    @Test
    void addProductShouldReturnNewBranchWithoutModifyingOriginal() {
        Branch updated = branch.addProduct(new Product("p3", "Gaseosa", 30));

        assertThat(updated.products()).hasSize(3);
        assertThat(branch.products()).hasSize(2);
    }

    @Test
    void addProductShouldFailWhenNameAlreadyExistsIgnoringCase() {
        assertThatThrownBy(() -> branch.addProduct(new Product("p3", "HAMBURGUESA", 10)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void removeProductShouldRemoveIt() {
        assertThat(branch.removeProduct("p1").products()).containsExactly(fries);
    }

    @Test
    void removeProductShouldFailWhenProductDoesNotExist() {
        assertThatThrownBy(() -> branch.removeProduct("no-existe"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProductShouldApplyTheGivenFunction() {
        Branch updated = branch.updateProduct("p1", product -> product.withStock(99));

        assertThat(updated.products()).contains(new Product("p1", "Hamburguesa", 99));
    }

    @Test
    void productWithMostStockShouldReturnTheHighest() {
        assertThat(branch.productWithMostStock()).contains(fries);
    }

    @Test
    void productWithMostStockShouldBeEmptyWhenBranchHasNoProducts() {
        assertThat(new Branch("b2", "Sur", List.of()).productWithMostStock()).isEmpty();
    }

    @Test
    void renameProductShouldFailWhenNameBelongsToAnotherProduct() {
        assertThatThrownBy(() -> branch.renameProduct("p1", "Papas"))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void renameProductShouldAllowChangingCaseOfItsOwnName() {
        Branch updated = branch.renameProduct("p1", "HAMBURGUESA");

        assertThat(updated.products().getFirst().name()).isEqualTo("HAMBURGUESA");
    }
}