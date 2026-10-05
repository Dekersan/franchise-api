package com.dekersan.franchise_api.infrastructure.entrypoint.web;

import com.dekersan.franchise_api.domain.exception.DuplicateResourceException;
import com.dekersan.franchise_api.domain.exception.ResourceNotFoundException;
import com.dekersan.franchise_api.domain.model.Franchise;
import com.dekersan.franchise_api.domain.model.TopStockProduct;
import com.dekersan.franchise_api.infrastructure.entrypoint.web.dto.NameRequest;
import com.dekersan.franchise_api.infrastructure.entrypoint.web.dto.ProductRequest;
import com.dekersan.franchise_api.infrastructure.entrypoint.web.dto.StockRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;
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

    private static final String TAG_FRANCHISES = "Franquicias";
    private static final String TAG_BRANCHES = "Sucursales";
    private static final String TAG_PRODUCTS = "Productos";

    @Bean
    @RouterOperations({
            @RouterOperation(path = FRANCHISES, method = RequestMethod.POST,
                    beanClass = FranchiseHandler.class, beanMethod = "createFranchise",
                    operation = @Operation(operationId = "createFranchise", tags = TAG_FRANCHISES,
                            summary = "Crear una franquicia",
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = NameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Franquicia creada",
                                            content = @Content(schema = @Schema(implementation = Franchise.class))),
                                    @ApiResponse(responseCode = "400", description = "Datos inválidos",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = "Ya existe una franquicia con ese nombre",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),

            @RouterOperation(path = FRANCHISE + "/name", method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class, beanMethod = "updateFranchiseName",
                    operation = @Operation(operationId = "updateFranchiseName", tags = TAG_FRANCHISES,
                            summary = "Actualizar el nombre de una franquicia",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true, description = "ID de la franquicia")
                            },
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = NameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Nombre actualizado",
                                            content = @Content(schema = @Schema(implementation = Franchise.class))),
                                    @ApiResponse(responseCode = "400", description = "Datos inválidos",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Franquicia no encontrada",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = "Ya existe una franquicia con ese nombre",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),

            @RouterOperation(path = FRANCHISE + "/top-stock-products", method = RequestMethod.GET,
                    beanClass = FranchiseHandler.class, beanMethod = "getTopStockProducts",
                    operation = @Operation(operationId = "getTopStockProducts", tags = TAG_FRANCHISES,
                            summary = "Producto con más stock por sucursal",
                            description = "Retorna, para cada sucursal de la franquicia, el producto con mayor stock. "
                                    + "Las sucursales sin productos no se incluyen.",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true, description = "ID de la franquicia")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Listado de productos con su sucursal",
                                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TopStockProduct.class)))),
                                    @ApiResponse(responseCode = "404", description = "Franquicia no encontrada",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),

            @RouterOperation(path = BRANCHES, method = RequestMethod.POST,
                    beanClass = FranchiseHandler.class, beanMethod = "addBranch",
                    operation = @Operation(operationId = "addBranch", tags = TAG_BRANCHES,
                            summary = "Agregar una sucursal a una franquicia",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true, description = "ID de la franquicia")
                            },
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = NameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Sucursal agregada",
                                            content = @Content(schema = @Schema(implementation = Franchise.class))),
                                    @ApiResponse(responseCode = "400", description = "Datos inválidos",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Franquicia no encontrada",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = "Ya existe una sucursal con ese nombre",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),

            @RouterOperation(path = BRANCH + "/name", method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class, beanMethod = "updateBranchName",
                    operation = @Operation(operationId = "updateBranchName", tags = TAG_BRANCHES,
                            summary = "Actualizar el nombre de una sucursal",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true, description = "ID de la franquicia"),
                                    @Parameter(in = ParameterIn.PATH, name = "branchId", required = true, description = "ID de la sucursal")
                            },
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = NameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Nombre actualizado",
                                            content = @Content(schema = @Schema(implementation = Franchise.class))),
                                    @ApiResponse(responseCode = "400", description = "Datos inválidos",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Franquicia o sucursal no encontrada",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = "Ya existe una sucursal con ese nombre",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),

            @RouterOperation(path = PRODUCTS, method = RequestMethod.POST,
                    beanClass = FranchiseHandler.class, beanMethod = "addProduct",
                    operation = @Operation(operationId = "addProduct", tags = TAG_PRODUCTS,
                            summary = "Agregar un producto a una sucursal",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true, description = "ID de la franquicia"),
                                    @Parameter(in = ParameterIn.PATH, name = "branchId", required = true, description = "ID de la sucursal")
                            },
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = ProductRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Producto agregado",
                                            content = @Content(schema = @Schema(implementation = Franchise.class))),
                                    @ApiResponse(responseCode = "400", description = "Datos inválidos",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Franquicia o sucursal no encontrada",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = "Ya existe un producto con ese nombre en la sucursal",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),

            @RouterOperation(path = PRODUCT, method = RequestMethod.DELETE,
                    beanClass = FranchiseHandler.class, beanMethod = "removeProduct",
                    operation = @Operation(operationId = "removeProduct", tags = TAG_PRODUCTS,
                            summary = "Eliminar un producto de una sucursal",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true, description = "ID de la franquicia"),
                                    @Parameter(in = ParameterIn.PATH, name = "branchId", required = true, description = "ID de la sucursal"),
                                    @Parameter(in = ParameterIn.PATH, name = "productId", required = true, description = "ID del producto")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "Producto eliminado"),
                                    @ApiResponse(responseCode = "404", description = "Franquicia, sucursal o producto no encontrado",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),

            @RouterOperation(path = PRODUCT + "/stock", method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class, beanMethod = "updateProductStock",
                    operation = @Operation(operationId = "updateProductStock", tags = TAG_PRODUCTS,
                            summary = "Modificar el stock de un producto",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true, description = "ID de la franquicia"),
                                    @Parameter(in = ParameterIn.PATH, name = "branchId", required = true, description = "ID de la sucursal"),
                                    @Parameter(in = ParameterIn.PATH, name = "productId", required = true, description = "ID del producto")
                            },
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = StockRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Stock actualizado",
                                            content = @Content(schema = @Schema(implementation = Franchise.class))),
                                    @ApiResponse(responseCode = "400", description = "Datos inválidos",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Franquicia, sucursal o producto no encontrado",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),

            @RouterOperation(path = PRODUCT + "/name", method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class, beanMethod = "updateProductName",
                    operation = @Operation(operationId = "updateProductName", tags = TAG_PRODUCTS,
                            summary = "Actualizar el nombre de un producto",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true, description = "ID de la franquicia"),
                                    @Parameter(in = ParameterIn.PATH, name = "branchId", required = true, description = "ID de la sucursal"),
                                    @Parameter(in = ParameterIn.PATH, name = "productId", required = true, description = "ID del producto")
                            },
                            requestBody = @RequestBody(required = true,
                                    content = @Content(schema = @Schema(implementation = NameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Nombre actualizado",
                                            content = @Content(schema = @Schema(implementation = Franchise.class))),
                                    @ApiResponse(responseCode = "400", description = "Datos inválidos",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Franquicia, sucursal o producto no encontrado",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                                    @ApiResponse(responseCode = "409", description = "Ya existe un producto con ese nombre en la sucursal",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            }))
    })
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