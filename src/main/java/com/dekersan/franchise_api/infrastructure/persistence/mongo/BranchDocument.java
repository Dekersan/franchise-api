package com.dekersan.franchise_api.infrastructure.persistence.mongo;

import java.util.List;

public record BranchDocument(String id, String name, List<ProductDocument> products) {
}