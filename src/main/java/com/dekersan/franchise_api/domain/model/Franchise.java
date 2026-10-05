package com.dekersan.franchise_api.domain.model;

import lombok.Builder;
import lombok.With;

import java.util.List;

@With
@Builder(toBuilder = true)
public record Franchise(String id, String name, List<Branch> branches) {

    public Franchise {
        branches = branches == null ? List.of() : List.copyOf(branches);
    }
}