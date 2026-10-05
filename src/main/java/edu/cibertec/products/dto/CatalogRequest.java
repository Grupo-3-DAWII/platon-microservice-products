package edu.cibertec.products.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CatalogRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 160, message = "Name must not exceed 160 characters")
        String name) {
}
