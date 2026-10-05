package com.dekersan.franchise_api.infrastructure.persistence.mongo;

import com.dekersan.franchise_api.domain.model.Franchise;
import com.dekersan.franchise_api.domain.port.FranchiseRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class FranchiseMongoAdapter implements FranchiseRepositoryPort {

    private final FranchiseMongoRepository repository;

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return repository.save(FranchiseMapper.toDocument(franchise))
                .map(FranchiseMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByNameAndIdNot(String name, String id) {
        return repository.existsByNameAndIdNot(name, id);
    }

    @Override
    public Mono<Franchise> findById(String id) {
        return repository.findById(id)
                .map(FranchiseMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return repository.existsByName(name);
    }
}