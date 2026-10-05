package com.dekersan.franchise_api.infrastructure.entrypoint.web;

import com.dekersan.franchise_api.application.usecase.FranchiseUseCase;
import com.dekersan.franchise_api.domain.model.TopStockProduct;
import com.dekersan.franchise_api.infrastructure.entrypoint.web.dto.NameRequest;
import com.dekersan.franchise_api.infrastructure.entrypoint.web.dto.ProductRequest;
import com.dekersan.franchise_api.infrastructure.entrypoint.web.dto.StockRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class FranchiseHandler {

    private static final String FRANCHISE_ID = "franchiseId";
    private static final String BRANCH_ID = "branchId";
    private static final String PRODUCT_ID = "productId";

    private final FranchiseUseCase franchiseUseCase;
    private final RequestValidator requestValidator;

    public Mono<ServerResponse> createFranchise(ServerRequest request) {
        return readBody(request, NameRequest.class)
                .flatMap(body -> franchiseUseCase.createFranchise(body.name()))
                .flatMap(franchise -> ServerResponse.status(HttpStatus.CREATED).bodyValue(franchise));
    }

    public Mono<ServerResponse> addBranch(ServerRequest request) {
        return readBody(request, NameRequest.class)
                .flatMap(body -> franchiseUseCase.addBranch(
                        request.pathVariable(FRANCHISE_ID),
                        body.name()))
                .flatMap(franchise -> ServerResponse.status(HttpStatus.CREATED).bodyValue(franchise));
    }

    public Mono<ServerResponse> addProduct(ServerRequest request) {
        return readBody(request, ProductRequest.class)
                .flatMap(body -> franchiseUseCase.addProduct(
                        request.pathVariable(FRANCHISE_ID),
                        request.pathVariable(BRANCH_ID),
                        body.name(),
                        body.stock()))
                .flatMap(franchise -> ServerResponse.status(HttpStatus.CREATED).bodyValue(franchise));
    }

    public Mono<ServerResponse> removeProduct(ServerRequest request) {
        return franchiseUseCase.removeProduct(
                        request.pathVariable(FRANCHISE_ID),
                        request.pathVariable(BRANCH_ID),
                        request.pathVariable(PRODUCT_ID))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> updateProductStock(ServerRequest request) {
        return readBody(request, StockRequest.class)
                .flatMap(body -> franchiseUseCase.updateProductStock(
                        request.pathVariable(FRANCHISE_ID),
                        request.pathVariable(BRANCH_ID),
                        request.pathVariable(PRODUCT_ID),
                        body.stock()))
                .flatMap(franchise -> ServerResponse.ok().bodyValue(franchise));
    }

    public Mono<ServerResponse> getTopStockProducts(ServerRequest request) {
        return franchiseUseCase.getTopStockProductByBranch(request.pathVariable(FRANCHISE_ID))
                .collectList()
                .flatMap(products -> ServerResponse.ok().bodyValue(products));
    }

    public Mono<ServerResponse> updateFranchiseName(ServerRequest request) {
        return readBody(request, NameRequest.class)
                .flatMap(body -> franchiseUseCase.updateFranchiseName(
                        request.pathVariable(FRANCHISE_ID),
                        body.name()))
                .flatMap(franchise -> ServerResponse.ok().bodyValue(franchise));
    }

    public Mono<ServerResponse> updateBranchName(ServerRequest request) {
        return readBody(request, NameRequest.class)
                .flatMap(body -> franchiseUseCase.updateBranchName(
                        request.pathVariable(FRANCHISE_ID),
                        request.pathVariable(BRANCH_ID),
                        body.name()))
                .flatMap(franchise -> ServerResponse.ok().bodyValue(franchise));
    }

    public Mono<ServerResponse> updateProductName(ServerRequest request) {
        return readBody(request, NameRequest.class)
                .flatMap(body -> franchiseUseCase.updateProductName(
                        request.pathVariable(FRANCHISE_ID),
                        request.pathVariable(BRANCH_ID),
                        request.pathVariable(PRODUCT_ID),
                        body.name()))
                .flatMap(franchise -> ServerResponse.ok().bodyValue(franchise));
    }

    private <T> Mono<T> readBody(ServerRequest request, Class<T> type) {
        return request.bodyToMono(type)
                .switchIfEmpty(Mono.error(new InvalidRequestException("El cuerpo de la petición es obligatorio")))
                .flatMap(requestValidator::validate);
    }
}