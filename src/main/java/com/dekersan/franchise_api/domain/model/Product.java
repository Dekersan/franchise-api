package com.dekersan.franchise_api.domain.model;

import lombok.Builder;
import lombok.With;

@With
@Builder(toBuilder = true)
public record Product(String id, String name, int stock) {
}