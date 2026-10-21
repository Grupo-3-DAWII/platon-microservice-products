package edu.cibertec.products.service;

import edu.cibertec.products.domain.entity.Product;
import edu.cibertec.products.domain.entity.Editorial;
import edu.cibertec.products.domain.entity.Genre;
import edu.cibertec.products.dto.PageResponse;
import edu.cibertec.products.dto.ProductRequest;
import edu.cibertec.products.dto.ProductResponse;

import edu.cibertec.products.exception.DuplicateResourceException;
import edu.cibertec.products.exception.ResourceNotFoundException;
import edu.cibertec.products.mapper.ProductMapper;
import edu.cibertec.products.repository.ProductRepository;
import edu.cibertec.products.repository.EditorialRepository;
import edu.cibertec.products.repository.GenreRepository;
import edu.cibertec.products.repository.StockMovementRepository;
import edu.cibertec.products.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Test
    void shouldUpdateDetailPreservingStockAndCreationDate() {
        Product product = product(1L, "Don Quijote", 5);
        var createdAt = java.time.LocalDateTime.of(2026, 1, 1, 0, 0);
        product.setCreatedAt(createdAt);
        stubCatalog();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        var detail = productService.updateDetail(1L, request("Updated", 0));

        assertThat(detail.name()).isEqualTo("Updated");
        assertThat(detail.salePrice()).isEqualByComparingTo("128.00");
        assertThat(detail.createdAt()).isEqualTo(createdAt);
        assertThat(detail.updatedAt()).isAfter(createdAt);
        assertThat(product.getStock()).isEqualTo(5);
        verify(productRepository).save(product);
        org.mockito.Mockito.verifyNoInteractions(stockMovementRepository);
    }

    @Test
    void shouldRejectUpdatingMissingProductDetail() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateDetail(999L, request()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldFindDetailWithoutLoadingStockMovements() {
        Product product = product(1L, "Don Quijote", 5);
        stubCatalog();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        var detail = productService.findDetailById(1L);

        assertThat(detail.name()).isEqualTo("Don Quijote");
        assertThat(detail.editorial()).isEqualTo("Alfaguara");
        assertThat(detail.genre()).isEqualTo("Novela");
        assertThat(detail.purchasePrice()).isEqualTo(product.getPurchasePrice());
        assertThat(detail.salePrice()).isEqualTo(product.getSalePrice());
        org.mockito.Mockito.verifyNoInteractions(stockMovementRepository);
    }

    @Test
    void shouldRejectMissingProductDetail() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findDetailById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Mock
    private ProductRepository productRepository;

    @Mock
    private EditorialRepository editorialRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Spy
    private ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void shouldFindProductsWithoutSearch() {
        Product product = product(1L, "Don Quijote", 5);
        stubCatalog();
        when(productRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1));

        PageResponse<ProductResponse> response = productService.findAll(PageRequest.of(0, 20), null);

        assertThat(response.content()).extracting(ProductResponse::name).containsExactly("Don Quijote");
        assertThat(response.content().get(0).available()).isTrue();
    }

    @Test
    void shouldFindProductsBySearch() {
        when(productRepository.findByNameContainingIgnoreCase(eq("quijote"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        assertThat(productService.findAll(PageRequest.of(0, 20), "  quijote  ").totalElements()).isZero();
    }

    @Test
    void shouldFindProductsWithBlankSearch() {
        when(productRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        assertThat(productService.findAll(PageRequest.of(0, 20), "   ").totalElements()).isZero();
    }

    @Test
    void shouldCreateProduct() {
        Product saved = product(1L, "Don Quijote", 5);
        stubCatalog();
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        assertThat(productService.create(request()).idProduct()).isEqualTo(1L);
    }

    @Test
    void shouldUpdateProduct() {
        Product product = product(1L, "Don Quijote", 5);
        stubCatalog();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        assertThat(productService.update(1L, request("Updated", 0)).available()).isTrue();
    }

    @Test
    void shouldFindAndDeleteProduct() {
        Product product = product(1L, "Don Quijote", 5);
        stubCatalog();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThat(productService.findById(1L).name()).isEqualTo("Don Quijote");
        productService.delete(1L);
        verify(productRepository).delete(product);
    }

    @Test
    void shouldRejectMissingProduct() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldRejectMissingCatalogEntry() {
        when(editorialRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(request()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldRejectMissingGenre() {
        when(editorialRepository.findById(1L)).thenReturn(Optional.of(Editorial.builder()
                .idEditorial(1L).name("Alfaguara").build()));
        when(genreRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(request()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldDefaultActiveFlagWhenMissing() {
        Product saved = product(1L, "Don Quijote", 5);
        stubCatalog();
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductRequest request = new ProductRequest("Don Quijote", "978-612-00-0001-1", "Miguel de Cervantes",
                1L, 1L, 1605, "Las aventuras del ingenioso hidalgo.", null,
                new BigDecimal("80.00"), new BigDecimal("60.00"), null);

        assertThat(productService.create(request).active()).isTrue();
    }

    @Test
    void shouldReturnProductWhenCatalogNamesAreMissing() {
        Product product = product(1L, "Don Quijote", 5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(editorialRepository.findById(1L)).thenReturn(Optional.empty());
        when(genreRepository.findById(1L)).thenReturn(Optional.empty());

        assertThat(productService.findById(1L).editorial()).isNull();
        assertThat(productService.findById(1L).genre()).isNull();
    }

    @Test
    void shouldRejectDuplicateNameOnCreateAndUpdate() {
        when(productRepository.existsByNameIgnoreCase("Don Quijote")).thenReturn(true);
        assertThatThrownBy(() -> productService.create(request(" Don Quijote ", 5)))
                .isInstanceOf(DuplicateResourceException.class);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product(1L, "Old", 1)));
        when(productRepository.existsByNameIgnoreCaseAndIdProductNot("Don Quijote", 1L)).thenReturn(true);
        assertThatThrownBy(() -> productService.update(1L, request()))
                .isInstanceOf(DuplicateResourceException.class);
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void shouldRejectDuplicateIsbn() {
        when(productRepository.existsByIsbnIgnoreCase("978-612-00-0001-1")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(request()))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void shouldCombineStockActiveAndSearchFilters() {
        Pageable pageable = PageRequest.of(0, 20);
        PageImpl<Product> empty = new PageImpl<>(List.of(), pageable, 0);
        when(productRepository.findByStockLessThanEqual(0, pageable)).thenReturn(empty);
        when(productRepository.findByActive(false, pageable)).thenReturn(empty);
        when(productRepository.findByStockLessThanEqualAndActive(5, true, pageable)).thenReturn(empty);
        when(productRepository.findByNameContainingIgnoreCaseAndStockLessThanEqual("book", 5, pageable)).thenReturn(empty);
        when(productRepository.findByNameContainingIgnoreCaseAndActive("book", true, pageable)).thenReturn(empty);
        when(productRepository.findByNameContainingIgnoreCaseAndStockLessThanEqualAndActive("book", 0, true, pageable)).thenReturn(empty);

        assertThat(productService.findAll(pageable, null, 0, null).totalElements()).isZero();
        assertThat(productService.findAll(pageable, null, null, false).totalElements()).isZero();
        assertThat(productService.findAll(pageable, null, 5, true).totalElements()).isZero();
        assertThat(productService.findAll(pageable, " book ", 5, null).totalElements()).isZero();
        assertThat(productService.findAll(pageable, "book", null, true).totalElements()).isZero();
        assertThat(productService.findAll(pageable, "book", 0, true).totalElements()).isZero();
        assertThatThrownBy(() -> productService.findAll(pageable, null, -1, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
    @Test
    void shouldIncludeFullDetailsAndLatestMovementInProductList() {
        Product product = product(1L, "Don Quijote", 5);
        java.time.LocalDateTime updatedAt = java.time.LocalDateTime.of(2026, 10, 19, 12, 0);
        product.setUpdatedAt(updatedAt);
        product.setCreatedAt(updatedAt.minusDays(1));
        stubCatalog();
        when(productRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1));
        when(stockMovementRepository.findFirstByIdProductOrderByMovementDateDescIdMovementDesc(1L))
                .thenReturn(Optional.of(edu.cibertec.products.domain.entity.StockMovement.builder()
                        .idMovement(10L).idProduct(1L).quantity(5).resultingStock(5).movementDate(updatedAt).build()));

        ProductResponse response = productService.findAll(PageRequest.of(0, 20), null).content().get(0);

        assertThat(response.updatedAt()).isEqualTo(updatedAt);
        assertThat(response.createdAt()).isEqualTo(updatedAt.minusDays(1));
        assertThat(response.lastMovement().idMovement()).isEqualTo(10L);
        assertThat(response.isbn()).isEqualTo(product.getIsbn());
        assertThat(response.purchasePrice()).isEqualByComparingTo("80.00");
        assertThat(response.editorial()).isEqualTo("Alfaguara");
        assertThat(response.genre()).isEqualTo("Novela");
    }
    private ProductRequest request() {
        return request("Don Quijote", 5);
    }

    private ProductRequest request(String name, Integer stock) {
        return new ProductRequest(name, "978-612-00-0001-1", "Miguel de Cervantes",
                1L, 1L, 1605, "Las aventuras del ingenioso hidalgo.", null,
                new BigDecimal("80.00"), new BigDecimal("60.00"), true);
    }

    private Product product(Long id, String name, Integer stock) {
        return Product.builder().idProduct(id).name(name).isbn("978-612-00-0001-1")
                .author("Miguel de Cervantes").idEditorial(1L).idGenre(1L).publicationYear(1605)
                .description("Las aventuras del ingenioso hidalgo.").purchasePrice(new BigDecimal("80.00"))
                .profitMargin(new BigDecimal("60.00")).salePrice(new BigDecimal("128.00"))
                .active(true).stock(stock).build();
    }

    private void stubCatalog() {
        when(editorialRepository.findById(1L)).thenReturn(Optional.of(Editorial.builder()
                .idEditorial(1L).name("Alfaguara").build()));
        when(genreRepository.findById(1L)).thenReturn(Optional.of(Genre.builder()
                .idGenre(1L).name("Novela").build()));
    }
}
