package com.dekersan.franchise_api.application.usecase;

import com.dekersan.franchise_api.domain.exception.DuplicateResourceException;
import com.dekersan.franchise_api.domain.exception.ResourceNotFoundException;
import com.dekersan.franchise_api.domain.model.Branch;
import com.dekersan.franchise_api.domain.model.Franchise;
import com.dekersan.franchise_api.domain.model.Product;
import com.dekersan.franchise_api.domain.model.TopStockProduct;
import com.dekersan.franchise_api.domain.port.FranchiseRepositoryPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.UnaryOperator;

@RequiredArgsConstructor
public class FranchiseUseCase {

    private final FranchiseRepositoryPort repository;

    public Mono<Franchise> createFranchise(String name) {
        return repository.existsByName(name)
                .flatMap(exists -> exists
                        ? Mono.error(new DuplicateResourceException(
                        "Ya existe una franquicia con el nombre '" + name + "'"))
                        : repository.save(Franchise.builder().id(newId()).name(name).build()));
    }

    public Mono<Franchise> addBranch(String franchiseId, String branchName) {
        return modifyFranchise(franchiseId, franchise ->
                franchise.addBranch(Branch.builder().id(newId()).name(branchName).build()));
    }

    public Mono<Franchise> addProduct(String franchiseId, String branchId, String productName, int stock) {
        return modifyBranch(franchiseId, branchId, branch ->
                branch.addProduct(Product.builder().id(newId()).name(productName).stock(stock).build()));
    }

    public Mono<Franchise> removeProduct(String franchiseId, String branchId, String productId) {
        return modifyBranch(franchiseId, branchId, branch -> branch.removeProduct(productId));
    }

    public Mono<Franchise> updateProductStock(String franchiseId, String branchId, String productId, int stock) {
        return modifyBranch(franchiseId, branchId, branch ->
                branch.updateProduct(productId, product -> product.withStock(stock)));
    }

    public Mono<Franchise> updateFranchiseName(String franchiseId, String newName) {
        return findFranchise(franchiseId)
                .flatMap(franchise -> repository.existsByNameAndIdNot(newName, franchiseId)
                        .flatMap(exists -> exists
                                ? Mono.<Franchise>error(new DuplicateResourceException(
                                "Ya existe una franquicia con el nombre '" + newName + "'"))
                                : repository.save(franchise.withName(newName))));
    }

    public Mono<Franchise> updateBranchName(String franchiseId, String branchId, String newName) {
        return modifyFranchise(franchiseId, franchise -> franchise.renameBranch(branchId, newName));
    }

    public Mono<Franchise> updateProductName(String franchiseId, String branchId, String productId, String newName) {
        return modifyBranch(franchiseId, branchId, branch -> branch.renameProduct(productId, newName));
    }

    public Flux<TopStockProduct> getTopStockProductByBranch(String franchiseId) {
        return findFranchise(franchiseId)
                .flatMapIterable(Franchise::topStockProductByBranch);
    }

    private Mono<Franchise> modifyBranch(String franchiseId, String branchId, UnaryOperator<Branch> operation) {
        return modifyFranchise(franchiseId, franchise -> franchise.updateBranch(branchId, operation));
    }

    private Mono<Franchise> modifyFranchise(String franchiseId, UnaryOperator<Franchise> operation) {
        return findFranchise(franchiseId)
                .map(operation)
                .flatMap(repository::save);
    }

    private Mono<Franchise> findFranchise(String franchiseId) {
        return repository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Franquicia no encontrada: " + franchiseId)));
    }

    private static String newId() {
        return UUID.randomUUID().toString();
    }
}