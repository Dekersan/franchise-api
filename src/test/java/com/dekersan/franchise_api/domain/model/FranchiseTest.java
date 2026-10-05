package com.dekersan.franchise_api.domain.model;

import com.dekersan.franchise_api.domain.exception.DuplicateResourceException;
import com.dekersan.franchise_api.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FranchiseTest {

    private final Branch centro = new Branch("b1", "Centro", List.of(
            new Product("p1", "Hamburguesa", 50),
            new Product("p2", "Papas", 80)));
    private final Branch norte = new Branch("b2", "Norte", List.of(
            new Product("p3", "Malteada", 40)));
    private final Branch sur = new Branch("b3", "Sur", List.of());
    private final Franchise franchise = new Franchise("f1", "Burger House", List.of(centro, norte, sur));

    @Test
    void topStockProductByBranchShouldReturnOneProductPerBranchAndSkipEmptyBranches() {
        assertThat(franchise.topStockProductByBranch()).containsExactly(
                new TopStockProduct("b1", "Centro", "p2", "Papas", 80),
                new TopStockProduct("b2", "Norte", "p3", "Malteada", 40));
    }

    @Test
    void addBranchShouldFailWhenNameAlreadyExists() {
        assertThatThrownBy(() -> franchise.addBranch(new Branch("b4", "centro", List.of())))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void updateBranchShouldFailWhenBranchDoesNotExist() {
        assertThatThrownBy(() -> franchise.updateBranch("no-existe", branch -> branch))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void renameBranchShouldChangeOnlyThatBranch() {
        Franchise updated = franchise.renameBranch("b2", "Norte Plaza");

        assertThat(updated.branches())
                .extracting(Branch::name)
                .containsExactly("Centro", "Norte Plaza", "Sur");
    }

    @Test
    void renameBranchShouldFailWhenNameBelongsToAnotherBranch() {
        assertThatThrownBy(() -> franchise.renameBranch("b2", "Sur"))
                .isInstanceOf(DuplicateResourceException.class);
    }
}