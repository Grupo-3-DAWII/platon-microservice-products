package edu.cibertec.products.dto;

public record CatalogResponse(Long id, String name, boolean active) {

    public CatalogResponse(Long id, String name) {
        this(id, name, true);
    }
}
