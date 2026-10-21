package edu.cibertec.products.service.impl;

import edu.cibertec.products.domain.entity.Product;
import edu.cibertec.products.dto.PageResponse;
import edu.cibertec.products.dto.ProductRequest;
import edu.cibertec.products.dto.ProductResponse;
import edu.cibertec.products.dto.ProductDetailResponse;

import edu.cibertec.products.dto.StockMovementResponse;
import edu.cibertec.products.domain.entity.StockMovement;
import edu.cibertec.products.repository.StockMovementRepository;
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
    private final StockMovementRepository stockMovementRepository;

    @Override
    public PageResponse<ProductResponse> findAll(Pageable pageable, String search) {
        return findAll(pageable, search, null, null);
    }

    @Override
    public PageResponse<ProductResponse> findAll(Pageable pageable, String search, Integer maxStock, Boolean active) {
        if (maxStock != null && maxStock < 0) {
            throw new IllegalArgumentException("maxStock must be greater than or equal to 0");
        }
        String name = search == null || search.isBlank() ? null : search.trim();
        Page<Product> productPage = findFiltered(pageable, name, maxStock, active);
        Page<ProductResponse> responsePage = productPage.map(this::toResponseWithMovement);

        return new PageResponse<>(responsePage.getContent(), responsePage.getNumber(), responsePage.getSize(),
                responsePage.getTotalElements(), responsePage.getTotalPages(), responsePage.isFirst(), responsePage.isLast());
    }

    private Page<Product> findFiltered(Pageable pageable, String name, Integer maxStock, Boolean active) {
        if (maxStock != null && active != null) {
            return name == null
                    ? productRepository.findByStockLessThanEqualAndActive(maxStock, active, pageable)
                    : productRepository.findByNameContainingIgnoreCaseAndStockLessThanEqualAndActive(name, maxStock, active, pageable);
        }
        if (maxStock != null) {
            return name == null
                    ? productRepository.findByStockLessThanEqual(maxStock, pageable)
                    : productRepository.findByNameContainingIgnoreCaseAndStockLessThanEqual(name, maxStock, pageable);
        }
        if (active != null) {
            return name == null
                    ? productRepository.findByActive(active, pageable)
                    : productRepository.findByNameContainingIgnoreCaseAndActive(name, active, pageable);
        }
        return name == null ? productRepository.findAll(pageable)
                : productRepository.findByNameContainingIgnoreCase(name, pageable);
    }
    @Override
    public ProductResponse findById(Long id) {
        return toResponseWithMovement(findProduct(id));
    }

    private ProductResponse toResponseWithMovement(Product product) {
        ProductResponse response = toResponse(product);
        return new ProductResponse(response.idProduct(), response.name(), response.isbn(), response.author(),
                response.editorialId(), response.editorial(), response.genreId(), response.genre(),
                response.publicationYear(), response.description(), response.image(), response.purchasePrice(),
                response.profitMargin(), response.salePrice(), response.active(), response.stock(), response.available(),
                response.createdAt(), response.updatedAt(), latestMovement(product.getIdProduct()));
    }

    @Override
    public ProductDetailResponse findDetailById(Long id) {
        return toDetailResponse(toResponse(findProduct(id)));
    }

    @Override
    @Transactional
    public ProductDetailResponse updateDetail(Long id, ProductRequest request) {
        return toDetailResponse(update(id, request));
    }

    private ProductDetailResponse toDetailResponse(ProductResponse product) {
        return new ProductDetailResponse(product.name(), product.isbn(), product.author(),
                product.editorial(), product.genre(), product.publicationYear(), product.description(),
                product.image(), product.purchasePrice(), product.salePrice(),
                product.createdAt(), product.updatedAt(), product.active());
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        ProductRequest normalizedRequest = normalize(request);
        ensureUnique(normalizedRequest, null);
        Product product = productMapper.toEntity(normalizedRequest);
        product.setStock(0);
        product.setCreatedAt(java.time.LocalDateTime.now());
        product.setUpdatedAt(product.getCreatedAt());
        applyCatalogAndPrice(product, normalizedRequest);
        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findProduct(id);
        ProductRequest normalizedRequest = normalize(request);
        ensureUnique(normalizedRequest, id);
        productMapper.updateEntity(product, normalizedRequest);
        product.setUpdatedAt(java.time.LocalDateTime.now());
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
                request.image(), request.purchasePrice(), request.profitMargin(), request.active());
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

    private StockMovementResponse latestMovement(Long productId) {
        return stockMovementRepository.findFirstByIdProductOrderByMovementDateDescIdMovementDesc(productId)
                .map(this::toMovementResponse).orElse(null);
    }

    private StockMovementResponse toMovementResponse(StockMovement movement) {
        return new StockMovementResponse(movement.getIdMovement(), movement.getIdProduct(), movement.getType(),
                movement.getQuantity(), movement.getMovementDate(), movement.getResultingStock(), movement.getNotes(),
                movement.getCreatedAt());
    }
}
