package com.dekersan.franchise_api.infrastructure.config;

import com.dekersan.franchise_api.application.usecase.FranchiseUseCase;
import com.dekersan.franchise_api.domain.port.FranchiseRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public FranchiseUseCase franchiseUseCase(FranchiseRepositoryPort franchiseRepositoryPort) {
        return new FranchiseUseCase(franchiseRepositoryPort);
    }
}