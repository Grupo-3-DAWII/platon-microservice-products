package edu.cibertec.products.dto;

public record ProductResponse(
        Long idProduct,
        String name,
        Integer stock,
        boolean available
) {
}
