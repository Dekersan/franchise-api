package com.dekersan.franchise_api.infrastructure.entrypoint.web;

public record ErrorResponse(int status, String error, String message) {
}