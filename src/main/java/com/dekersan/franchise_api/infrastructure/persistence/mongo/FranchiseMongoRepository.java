package com.dekersan.franchise_api.infrastructure.persistence.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface FranchiseMongoRepository extends ReactiveMongoRepository<FranchiseDocument, String> {

    Mono<Boolean> existsByNameAndIdNot(String name, String id);

    Mono<Boolean> existsByName(String name);
}