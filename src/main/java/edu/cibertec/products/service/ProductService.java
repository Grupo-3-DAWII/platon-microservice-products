package edu.cibertec.products.service;

import edu.cibertec.products.dto.PageResponse;
import edu.cibertec.products.dto.ProductRequest;
import edu.cibertec.products.dto.ProductResponse;
import edu.cibertec.products.dto.ProductDetailResponse;

import org.springframework.data.domain.Pageable;

public interface ProductService {

    PageResponse<ProductResponse> findAll(Pageable pageable, String search);

    PageResponse<ProductResponse> findAll(Pageable pageable, String search, Integer maxStock, Boolean active);

    ProductResponse findById(Long id);

    ProductDetailResponse findDetailById(Long id);

    ProductDetailResponse updateDetail(Long id, ProductRequest request);

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);
}
