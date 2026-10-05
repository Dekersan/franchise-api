package com.dekersan.franchise_api.domain.model;

import com.dekersan.franchise_api.domain.exception.DuplicateResourceException;
import com.dekersan.franchise_api.domain.exception.ResourceNotFoundException;
import lombok.Builder;
import lombok.With;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@With
@Builder(toBuilder = true)
public record Branch(String id, String name, List<Product> products) {

    public Branch {
        products = products == null ? List.of() : List.copyOf(products);
    }

    public Branch addProduct(Product product) {
        if (hasProductNamed(product.name())) {
            throw new DuplicateResourceException(
                    "Ya existe un producto con el nombre '" + product.name() + "' en la sucursal");
        }
        return withProducts(Stream.concat(products.stream(), Stream.of(product)).toList());
    }

    public Branch removeProduct(String productId) {
        requireProduct(productId);
        return withProducts(products.stream()
                .filter(product -> !product.id().equals(productId))
                .toList());
    }

    public Branch updateProduct(String productId, UnaryOperator<Product> updater) {
        requireProduct(productId);
        return withProducts(products.stream()
                .map(product -> product.id().equals(productId) ? updater.apply(product) : product)
                .toList());
    }

    public Optional<Product> productWithMostStock() {
        return products.stream().max(Comparator.comparingInt(Product::stock));
    }

    private boolean hasProductNamed(String productName) {
        return products.stream().anyMatch(product -> product.name().equalsIgnoreCase(productName));
    }

    private void requireProduct(String productId) {
        if (products.stream().noneMatch(product -> product.id().equals(productId))) {
            throw new ResourceNotFoundException("Producto no encontrado: " + productId);
        }
    }
}