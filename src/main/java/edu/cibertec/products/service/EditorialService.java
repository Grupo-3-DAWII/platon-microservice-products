package edu.cibertec.products.service;

import edu.cibertec.products.dto.CatalogResponse;
import edu.cibertec.products.dto.CatalogRequest;

import java.util.List;

public interface EditorialService {
    List<CatalogResponse> findAll();
    CatalogResponse findById(Long id);
    CatalogResponse create(CatalogRequest request);
    CatalogResponse update(Long id, CatalogRequest request);
    void delete(Long id);
}
