package com.dekersan.franchise_api.domain.port;

import com.dekersan.franchise_api.domain.model.Franchise;
import reactor.core.publisher.Mono;

public interface FranchiseRepositoryPort {

    Mono<Franchise> save(Franchise franchise);

    Mono<Franchise> findById(String id);

    Mono<Boolean> existsByName(String name);
}