package com.dekersan.franchise_api.domain.model;

public record TopStockProduct(
        String branchId,
        String branchName,
        String productId,
        String productName,
        int stock
) {
}