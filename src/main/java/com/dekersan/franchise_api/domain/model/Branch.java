package com.dekersan.franchise_api.domain.model;

import lombok.Builder;
import lombok.With;

import java.util.List;

@With
@Builder(toBuilder = true)
public record Branch(String id, String name, List<Product> products) {

    public Branch {
        products = products == null ? List.of() : List.copyOf(products);
    }
}