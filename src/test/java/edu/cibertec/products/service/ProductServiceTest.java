package edu.cibertec.products.service;

import edu.cibertec.products.domain.entity.Product;
import edu.cibertec.products.dto.PageResponse;
import edu.cibertec.products.dto.ProductRequest;
import edu.cibertec.products.dto.ProductResponse;
import edu.cibertec.products.exception.DuplicateResourceException;
import edu.cibertec.products.exception.ResourceNotFoundException;
import edu.cibertec.products.mapper.ProductMapper;
import edu.cibertec.products.repository.ProductRepository;
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

    @Spy
    private ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void shouldFindProductsWithoutSearch() {
        Product product = product(1L, "Don Quijote", 5);
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
    void shouldCreateProduct() {
        Product saved = product(1L, "Don Quijote", 5);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        assertThat(productService.create(request()).idProduct()).isEqualTo(1L);
    }

    @Test
    void shouldUpdateProduct() {
        Product product = product(1L, "Don Quijote", 5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        assertThat(productService.update(1L, new ProductRequest("Updated", 0)).available()).isFalse();
    }

    @Test
    void shouldFindAndDeleteProduct() {
        Product product = product(1L, "Don Quijote", 5);
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
    void shouldRejectDuplicateNameOnCreateAndUpdate() {
        when(productRepository.existsByNameIgnoreCase("Don Quijote")).thenReturn(true);
        assertThatThrownBy(() -> productService.create(new ProductRequest(" Don Quijote ", 5)))
                .isInstanceOf(DuplicateResourceException.class);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product(1L, "Old", 1)));
        when(productRepository.existsByNameIgnoreCaseAndIdProductNot("Don Quijote", 1L)).thenReturn(true);
        assertThatThrownBy(() -> productService.update(1L, request()))
                .isInstanceOf(DuplicateResourceException.class);
        verify(productRepository, never()).delete(any(Product.class));
    }

    private ProductRequest request() {
        return new ProductRequest("Don Quijote", 5);
    }

    private Product product(Long id, String name, Integer stock) {
        return Product.builder().idProduct(id).name(name).stock(stock).build();
    }
}
