package com.dekersan.franchise_api.infrastructure.persistence.mongo;

import com.dekersan.franchise_api.domain.model.Branch;
import com.dekersan.franchise_api.domain.model.Franchise;
import com.dekersan.franchise_api.domain.model.Product;

import java.util.List;
import java.util.Optional;

public final class FranchiseMapper {

    private FranchiseMapper() {
    }

    public static FranchiseDocument toDocument(Franchise franchise) {
        return new FranchiseDocument(
                franchise.id(),
                franchise.name(),
                franchise.branches().stream().map(FranchiseMapper::toDocument).toList()
        );
    }

    public static Franchise toDomain(FranchiseDocument document) {
        return new Franchise(
                document.id(),
                document.name(),
                Optional.ofNullable(document.branches()).orElse(List.of())
                        .stream().map(FranchiseMapper::toDomain).toList()
        );
    }

    private static BranchDocument toDocument(Branch branch) {
        return new BranchDocument(
                branch.id(),
                branch.name(),
                branch.products().stream().map(FranchiseMapper::toDocument).toList()
        );
    }

    private static Branch toDomain(BranchDocument document) {
        return new Branch(
                document.id(),
                document.name(),
                Optional.ofNullable(document.products()).orElse(List.of())
                        .stream().map(FranchiseMapper::toDomain).toList()
        );
    }

    private static ProductDocument toDocument(Product product) {
        return new ProductDocument(product.id(), product.name(), product.stock());
    }

    private static Product toDomain(ProductDocument document) {
        return new Product(document.id(), document.name(), document.stock());
    }
}