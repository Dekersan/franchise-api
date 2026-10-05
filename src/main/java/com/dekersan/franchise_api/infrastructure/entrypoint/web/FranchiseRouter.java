package com.dekersan.franchise_api.infrastructure.entrypoint.web;

import com.dekersan.franchise_api.domain.exception.DuplicateResourceException;
import com.dekersan.franchise_api.domain.exception.ResourceNotFoundException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@Configuration
public class FranchiseRouter {

    private static final String FRANCHISES = "/api/franchises";
    private static final String FRANCHISE = FRANCHISES + "/{franchiseId}";
    private static final String BRANCHES = FRANCHISE + "/branches";
    private static final String BRANCH = BRANCHES + "/{branchId}";
    private static final String PRODUCTS = BRANCH + "/products";
    private static final String PRODUCT = PRODUCTS + "/{productId}";

    @Bean
    public RouterFunction<ServerResponse> franchiseRoutes(FranchiseHandler handler) {
        return RouterFunctions.route()
                .POST(FRANCHISES, handler::createFranchise)
                .POST(BRANCHES, handler::addBranch)
                .POST(PRODUCTS, handler::addProduct)
                .DELETE(PRODUCT, handler::removeProduct)
                .PATCH(PRODUCT + "/stock", handler::updateProductStock)
                .GET(FRANCHISE + "/top-stock-products", handler::getTopStockProducts)
                .PATCH(FRANCHISE + "/name", handler::updateFranchiseName)
                .PATCH(BRANCH + "/name", handler::updateBranchName)
                .PATCH(PRODUCT + "/name", handler::updateProductName)
                .onError(ResourceNotFoundException.class,
                        (error, request) -> errorResponse(HttpStatus.NOT_FOUND, error.getMessage()))
                .onError(DuplicateResourceException.class,
                        (error, request) -> errorResponse(HttpStatus.CONFLICT, error.getMessage()))
                .onError(InvalidRequestException.class,
                        (error, request) -> errorResponse(HttpStatus.BAD_REQUEST, error.getMessage()))
                .onError(ServerWebInputException.class,
                        (error, request) -> errorResponse(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido"))
                .build();
    }

    private static Mono<ServerResponse> errorResponse(HttpStatus status, String message) {
        return ServerResponse.status(status)
                .bodyValue(new ErrorResponse(status.value(), status.getReasonPhrase(), message));
    }
}