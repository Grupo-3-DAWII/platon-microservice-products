package edu.cibertec.products.service.impl;

import edu.cibertec.products.domain.entity.Product;
import edu.cibertec.products.dto.PageResponse;
import edu.cibertec.products.dto.ProductRequest;
import edu.cibertec.products.dto.ProductResponse;
import edu.cibertec.products.exception.DuplicateResourceException;
import edu.cibertec.products.exception.ResourceNotFoundException;
import edu.cibertec.products.mapper.ProductMapper;
import edu.cibertec.products.repository.ProductRepository;
import edu.cibertec.products.repository.EditorialRepository;
import edu.cibertec.products.repository.GenreRepository;
import edu.cibertec.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final EditorialRepository editorialRepository;
    private final GenreRepository genreRepository;

    @Override
    public PageResponse<ProductResponse> findAll(Pageable pageable, String search) {
        Page<Product> productPage = Objects.isNull(search) || search.isBlank()
                ? productRepository.findAll(pageable)
                : productRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        Page<ProductResponse> responsePage = productPage.map(this::toResponse);

        return new PageResponse<>(responsePage.getContent(), responsePage.getNumber(), responsePage.getSize(),
                responsePage.getTotalElements(), responsePage.getTotalPages(), responsePage.isFirst(), responsePage.isLast());
    }

    @Override
    public ProductResponse findById(Long id) {
        return toResponse(findProduct(id));
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        ProductRequest normalizedRequest = normalize(request);
        ensureUnique(normalizedRequest, null);
        Product product = productMapper.toEntity(normalizedRequest);
        applyCatalogAndPrice(product, normalizedRequest);
        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findProduct(id);
        ProductRequest normalizedRequest = normalize(request);
        ensureUnique(normalizedRequest, id);
        productMapper.updateEntity(product, normalizedRequest);
        applyCatalogAndPrice(product, normalizedRequest);
        return toResponse(productRepository.save(product));
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

    private void ensureUnique(ProductRequest request, Long idProduct) {
        boolean exists = Objects.nonNull(idProduct)
                ? productRepository.existsByNameIgnoreCaseAndIdProductNot(request.name(), idProduct)
                : productRepository.existsByNameIgnoreCase(request.name());
        boolean isbnExists = Objects.nonNull(idProduct)
                ? productRepository.existsByIsbnIgnoreCaseAndIdProductNot(request.isbn(), idProduct)
                : productRepository.existsByIsbnIgnoreCase(request.isbn());
        if (exists) {
            throw new DuplicateResourceException("Product name already exists");
        }
        if (isbnExists) {
            throw new DuplicateResourceException("Product ISBN already exists");
        }
    }

    private ProductRequest normalize(ProductRequest request) {
        return new ProductRequest(request.name().trim(), request.isbn().trim(), request.author().trim(),
                request.editorialId(), request.genreId(), request.publicationYear(), request.description().trim(),
                request.image(), request.imageContentType(), request.purchasePrice(), request.profitMargin(),
                request.active(), request.stock());
    }

    private void applyCatalogAndPrice(Product product, ProductRequest request) {
        product.setIdEditorial(editorialRepository.findById(request.editorialId())
                .orElseThrow(() -> new ResourceNotFoundException("Editorial not found")).getIdEditorial());
        product.setIdGenre(genreRepository.findById(request.genreId())
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found")).getIdGenre());
        product.setSalePrice(calculateSalePrice(request.purchasePrice(), request.profitMargin()));
        if (Objects.isNull(product.getActive())) {
            product.setActive(Boolean.TRUE);
        }
    }

    private BigDecimal calculateSalePrice(BigDecimal purchasePrice, BigDecimal profitMargin) {
        BigDecimal multiplier = BigDecimal.ONE.add(profitMargin.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        return purchasePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    private ProductResponse toResponse(Product product) {
        String editorialName = editorialRepository.findById(product.getIdEditorial())
                .map(editorial -> editorial.getName()).orElse(null);
        String genreName = genreRepository.findById(product.getIdGenre())
                .map(genre -> genre.getName()).orElse(null);
        return productMapper.toResponse(product, editorialName, genreName);
    }
}
