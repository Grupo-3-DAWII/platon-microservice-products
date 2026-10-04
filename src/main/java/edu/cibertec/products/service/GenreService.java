package edu.cibertec.products.service;

import edu.cibertec.products.dto.CatalogResponse;

import java.util.List;

public interface GenreService {
    List<CatalogResponse> findAll();
}
