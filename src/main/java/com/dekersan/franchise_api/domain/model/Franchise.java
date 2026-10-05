package com.dekersan.franchise_api.domain.model;

import com.dekersan.franchise_api.domain.exception.DuplicateResourceException;
import com.dekersan.franchise_api.domain.exception.ResourceNotFoundException;
import lombok.Builder;
import lombok.With;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@With
@Builder(toBuilder = true)
public record Franchise(String id, String name, List<Branch> branches) {

    public Franchise {
        branches = branches == null ? List.of() : List.copyOf(branches);
    }

    public Franchise addBranch(Branch branch) {
        if (hasBranchNamed(branch.name())) {
            throw new DuplicateResourceException(
                    "Ya existe una sucursal con el nombre '" + branch.name() + "' en la franquicia");
        }
        return withBranches(Stream.concat(branches.stream(), Stream.of(branch)).toList());
    }

    public Franchise updateBranch(String branchId, UnaryOperator<Branch> updater) {
        requireBranch(branchId);
        return withBranches(branches.stream()
                .map(branch -> branch.id().equals(branchId) ? updater.apply(branch) : branch)
                .toList());
    }

    public Franchise renameBranch(String branchId, String newName) {
        requireBranch(branchId);
        boolean nameTaken = branches.stream()
                .anyMatch(branch -> !branch.id().equals(branchId)
                        && branch.name().equalsIgnoreCase(newName));
        if (nameTaken) {
            throw new DuplicateResourceException(
                    "Ya existe una sucursal con el nombre '" + newName + "' en la franquicia");
        }
        return updateBranch(branchId, branch -> branch.withName(newName));
    }

    public List<TopStockProduct> topStockProductByBranch() {
        return branches.stream()
                .flatMap(branch -> branch.productWithMostStock()
                        .map(product -> new TopStockProduct(
                                branch.id(),
                                branch.name(),
                                product.id(),
                                product.name(),
                                product.stock()))
                        .stream())
                .toList();
    }

    private boolean hasBranchNamed(String branchName) {
        return branches.stream().anyMatch(branch -> branch.name().equalsIgnoreCase(branchName));
    }

    private void requireBranch(String branchId) {
        if (branches.stream().noneMatch(branch -> branch.id().equals(branchId))) {
            throw new ResourceNotFoundException("Sucursal no encontrada: " + branchId);
        }
    }
}