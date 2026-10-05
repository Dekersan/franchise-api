package com.dekersan.franchise_api.infrastructure.persistence.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "franchises")
public record FranchiseDocument(@Id String id, String name, List<BranchDocument> branches) {
}