package edu.cibertec.products.service.impl;

import edu.cibertec.products.domain.entity.Product;
import edu.cibertec.products.dto.PageResponse;
import edu.cibertec.products.dto.ProductRequest;
import edu.cibertec.products.dto.ProductResponse;
import edu.cibertec.products.exception.DuplicateResourceException;
import edu.cibertec.products.exception.ResourceNotFoundException;
import edu.cibertec.products.mapper.ProductMapper;
import edu.cibertec.products.repository.ProductRepository;
import edu.cibertec.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public PageResponse<ProductResponse> findAll(Pageable pageable, String search) {
        Page<Product> productPage = Objects.isNull(search) || search.isBlank()
                ? productRepository.findAll(pageable)
                : productRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        Page<ProductResponse> responsePage = productPage.map(productMapper::toResponse);

        return new PageResponse<>(responsePage.getContent(), responsePage.getNumber(), responsePage.getSize(),
                responsePage.getTotalElements(), responsePage.getTotalPages(), responsePage.isFirst(), responsePage.isLast());
    }

    @Override
    public ProductResponse findById(Long id) {
        return productMapper.toResponse(findProduct(id));
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        ProductRequest normalizedRequest = normalize(request);
        ensureUnique(normalizedRequest.name(), null);
        return productMapper.toResponse(productRepository.save(productMapper.toEntity(normalizedRequest)));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findProduct(id);
        ProductRequest normalizedRequest = normalize(request);
        ensureUnique(normalizedRequest.name(), id);
        productMapper.updateEntity(product, normalizedRequest);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        productRepository.delete(findProduct(id));
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private void ensureUnique(String name, Long idProduct) {
        boolean exists = Objects.nonNull(idProduct)
                ? productRepository.existsByNameIgnoreCaseAndIdProductNot(name, idProduct)
                : productRepository.existsByNameIgnoreCase(name);
        if (exists) {
            throw new DuplicateResourceException("Product name already exists");
        }
    }

    private ProductRequest normalize(ProductRequest request) {
        return new ProductRequest(request.name().trim(), request.stock());
    }
}
