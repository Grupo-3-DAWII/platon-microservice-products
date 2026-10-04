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

    @Mock
    private ProductRepository productRepository;

    @Mock
    private EditorialRepository editorialRepository;

    @Mock
    private GenreRepository genreRepository;

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

        assertThat(productService.update(1L, request("Updated", 0)).available()).isFalse();
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
                1L, 1L, 1605, "Las aventuras del ingenioso hidalgo.", null, null,
                new BigDecimal("80.00"), new BigDecimal("60.00"), null, 5);

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

    private ProductRequest request() {
        return request("Don Quijote", 5);
    }

    private ProductRequest request(String name, Integer stock) {
        return new ProductRequest(name, "978-612-00-0001-1", "Miguel de Cervantes",
                1L, 1L, 1605, "Las aventuras del ingenioso hidalgo.", null, null,
                new BigDecimal("80.00"), new BigDecimal("60.00"), true, stock);
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
